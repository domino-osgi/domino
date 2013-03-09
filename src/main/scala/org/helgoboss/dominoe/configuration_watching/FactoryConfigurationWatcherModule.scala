package org.helgoboss.dominoe.configuration_watching

import org.osgi.service.cm.{Configuration, ConfigurationAdmin, ManagedServiceFactory}
import org.helgoboss.module_support.{ModuleContext, ModuleContainer, Module}
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider}
import org.helgoboss.scala_osgi_metatype.interfaces.MetaTypeProvider
import org.osgi.framework.{BundleContext, Constants, ServiceRegistration}
import org.helgoboss.scala_osgi_metatype.adapters.MetaTypeProviderAdapter
import java.util.Dictionary
import org.helgoboss.dominoe.DominoeUtil
import org.helgoboss.dominoe.service_consuming.ServiceConsumer


class FactoryConfigurationWatcherModule(
    servicePid: String,
    name: String,
    f: (Option[Map[String, Any]], String) => Unit,
    metaTypeProvider: Option[MetaTypeProvider],
    serviceConsumer: ServiceConsumer,
    bundleContext: BundleContext,
    moduleContext: ModuleContext) extends ManagedServiceFactory with Module with JMetaTypeProvider {

  var reg: ServiceRegistration = _

  lazy val metaTypeProviderAdapter = metaTypeProvider map { new MetaTypeProviderAdapter(_) }

  lazy val interfacesArray: Array[String] = Array(classOf[ManagedServiceFactory].getName) ++ (
    metaTypeProvider map { p => classOf[JMetaTypeProvider].getName }
    )

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
    reg = bundleContext.registerService(interfacesArray, this, DominoeUtil.convertToDictionary(propertiesMap))
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
          f(Some(DominoeUtil.convertToMap(conf)), pid)

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

  def getObjectClassDefinition(id: String, locale: String) = {
    metaTypeProviderAdapter map { _.getObjectClassDefinition(id, locale) } orNull
  }

  def getLocales = metaTypeProviderAdapter map { _.getLocales } orNull
}