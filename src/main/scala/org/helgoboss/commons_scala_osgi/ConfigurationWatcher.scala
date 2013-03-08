package org.helgoboss.commons_scala_osgi

import org.osgi.framework.{ BundleContext, Constants, ServiceRegistration }
import org.helgoboss.module_support._
import org.osgi.service.cm.{ManagedService, ManagedServiceFactory, ConfigurationAdmin, Configuration}
import java.util.{ Dictionary, Hashtable, Vector }

class SimpleConfigurationWatcher(
    protected val moduleContext: ModuleContext,
    protected val bundleContext: BundleContext,
    protected val serviceConsumer: ServiceConsumer) extends ConfigurationWatcher {

  def this(osgiContext: OsgiContext, serviceConsumer: ServiceConsumer) = this(osgiContext, osgiContext.bundleContext, serviceConsumer)
  
  def this(convenientBundleActivator: ConvenientBundleActivator) = this(
    convenientBundleActivator,
    convenientBundleActivator.bundleContext,
    convenientBundleActivator)
}

trait ConfigurationWatcher {
  protected def moduleContext: ModuleContext
  protected def bundleContext: BundleContext
  protected def serviceConsumer: ServiceConsumer

  def whenConfigurationUpdated(servicePid: String)(f: (Option[Map[String, Any]]) => Unit): ServiceRegistration = {
    val s = new ConfigurationWatcherService(servicePid, f)
    moduleContext.addModule(s)
    s.reg
  }
  
  def whenFactoryConfigurationUpdated(servicePid: String, name: String)(f: (Option[Map[String, Any]], String) => Unit): ServiceRegistration = {
  
    val s = new FactoryConfigurationWatcherService(servicePid, name, f)
    moduleContext.addModule(s)
    s.reg
  }

  private def convertToDictionary(map: Map[String, Any]): Dictionary[String, AnyRef] = {
    val table = new Hashtable[String, AnyRef]
    map.foreach {
      case (key, value) =>
        table.put(key, value.asInstanceOf[AnyRef])
    }
    table
  }

  private def convertToMap(dictionary: Dictionary[_, _]): Map[String, Any] = {
    val map = new collection.mutable.HashMap[String, Any]
    import collection.JavaConversions._
    dictionary.keys.foreach { key =>
      val jValue = dictionary.get(key)
      val value = jValue match {
        case v: Vector[_] => v.toList
        case a: Array[_] => a.toList
        case _ => jValue
      }
      map(key.asInstanceOf[String]) = value.asInstanceOf[Any]
    }
    map.toMap
  }

  private class ConfigurationWatcherService(servicePid: String, 
      f: Option[Map[String, Any]] => Unit) extends ManagedService with Module {

    var reg: ServiceRegistration = _

    var moduleContainer: Option[ModuleContainer] = None
    
    var oldOptConf: Option[Dictionary[_, _]] = None

    def start() {
      val propertiesMap = Map(Constants.SERVICE_PID -> servicePid)
      
      // At first execute inner block synchronously with current configuration.
      val optConf = serviceConsumer.withService[ConfigurationAdmin, Option[Dictionary[_, _]]] {
        case Some(confAdmin) =>
          Option(confAdmin.getConfiguration(servicePid).getProperties)
          
        case None => 
          None
      }
      executeBlockWithConf(optConf)
      
      /* Then register managed service. This will cause ConfigurationAdmin push the current configuration in a separate thread
         and call updated(). In updated(), we prevent the execution of the inner block, if the configuration stayed the same. */
      reg = bundleContext.registerService(classOf[ManagedService].getName, this, convertToDictionary(propertiesMap))
    }

    def stop() {
      moduleContainer foreach { _.stop() }
      reg.unregister()
      reg = null
    }

    def updated(conf: Dictionary[_, _]) {
      // See http://www.mail-archive.com/users@felix.apache.org/msg06764.html
      // We really need the right webservice URL here. This might be already important on first OSGi startup.
      // Therefore we query the config admin directly because the user can make sure then that the config value is already set.
      val safeOptConf = Option(conf) orElse getConfigDirectly()
      
      executeBlockWithConfIfChanged(safeOptConf)
    }
    
    private def executeBlockWithConfIfChanged(optConf: Option[Dictionary[_, _]]) {
      if (oldOptConf != optConf) {
        executeBlockWithConf(optConf)
      }
    }
    
    private def executeBlockWithConf(optConf: Option[Dictionary[_, _]]) {
      // Stop previous modules
      moduleContainer foreach { _.stop() }
      
      // Start new modules
      moduleContainer = Some(moduleContext.executeWithinNewModuleContainer {
        optConf match {
          case Some(conf) =>
            f(Some(convertToMap(conf)))
            
          case None =>
            f(None)
        }
      })
      
      // Save old conf
      oldOptConf = optConf
    }
      
    private def getConfigDirectly(): Option[Dictionary[_, _]] = {
      serviceConsumer.withService[ConfigurationAdmin, Option[Dictionary[_, _]]] {
        case Some(confAdmin) =>
          Option(confAdmin.getConfiguration(servicePid)) match {
            case Some(c) => Option(c.getProperties)
            case None => None
          }
          
        case None => None
      }
    }
  }
  
  private class FactoryConfigurationWatcherService(servicePid: String,
      name: String,
      f: (Option[Map[String, Any]], String) => Unit) extends ManagedServiceFactory with Module {

    var reg: ServiceRegistration = _

    var moduleContainers = new collection.mutable.HashMap[String, ModuleContainer]
    var oldOptConfs = new collection.mutable.HashMap[String, Option[Dictionary[_, _]]]

    def start() {
      val propertiesMap = Map(Constants.SERVICE_PID -> servicePid)
      
      // At first execute inner block synchronously with each configuration
      
      val configurations = serviceConsumer.withService[ConfigurationAdmin, Traversable[Configuration]] {
        case Some(confAdmin) =>
          Option(confAdmin.listConfigurations("(service.pid=" + servicePid + ")")) match {
            case Some(cs) => cs
            case None => Nil
          }
          
        case None => 
          Nil
      }
      
      configurations foreach { configuration =>
        executeBlockWithConf(configuration.getFactoryPid, Option(configuration.getProperties))
      }
      
      // Then register managed service factory
      reg = bundleContext.registerService(classOf[ManagedServiceFactory].getName, this, convertToDictionary(propertiesMap))
    }

    def stop() {
      moduleContainers.keys foreach { stopAndRemoveModuleContainer }
      reg.unregister()
      reg = null
    }
    
    def getName = name

    def updated(pid: String, conf: Dictionary[_, _]) {
      executeBlockWithConfIfChanged(pid, Option(conf))
    }
    
    
    private def executeBlockWithConfIfChanged(pid: String, optConf: Option[Dictionary[_, _]]) {
      if (oldOptConfs.get(pid) != Some(optConf)) {
        executeBlockWithConf(pid, optConf)
      }
    }
    
    private def executeBlockWithConf(pid: String, optConf: Option[Dictionary[_, _]]) {
      if (moduleContainers.contains(pid)) {
        // Existing service is reconfigured. So we have to stop the module corresponding to the PID first.
        stopAndRemoveModuleContainer(pid)
      }
      val newModuleContainer = moduleContext.executeWithinNewModuleContainer {
        optConf match {
          case Some(conf) =>
            f(Some(convertToMap(conf)), pid)
          
          case None =>
            f(None, pid)
        }
      }
      addModuleContainer(pid, newModuleContainer, optConf)
    }
    
    def deleted(pid: String) {
      stopAndRemoveModuleContainer(pid)
    }
    
    private def addModuleContainer(pid: String, moduleContainer: ModuleContainer, optConf: Option[Dictionary[_, _]]) {
      moduleContainers += (pid -> moduleContainer)
      oldOptConfs += (pid -> optConf)
    }    
    
    private def stopAndRemoveModuleContainer(pid: String) {
      val moduleContainer = moduleContainers(pid)
      moduleContainer.stop()
      moduleContainers -= pid
      oldOptConfs -= pid
    }    
  }
}