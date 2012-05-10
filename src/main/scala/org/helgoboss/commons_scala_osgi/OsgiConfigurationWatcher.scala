package org.helgoboss.commons_scala_osgi

import org.osgi.framework.{BundleContext, BundleActivator, ServiceReference, Constants, ServiceRegistration}
import org.osgi.util.tracker.ServiceTracker
import org.helgoboss.module_support.{Module, ModuleContainer}
import org.osgi.service.cm.ManagedService
import java.util.{Dictionary, Hashtable, Vector}

trait OsgiConfigurationWatcher extends OsgiContext with OsgiProvider {

    protected def whenConfigurationUpdated(servicePid: String)(f: (Option[Map[String, Any]]) => Unit): ServiceRegistration = {
        val s = new ConfigurationWatcherService(servicePid, f)
        addModule(s)
        s.reg
    }
        
    private def convertToDictionary(map: Map[String, Any]): Dictionary[String, AnyRef] = {
        val table = new Hashtable[String, AnyRef]
        map.foreach { case (key, value) =>
            table.put(key, value.asInstanceOf[AnyRef])
        }
        table
    }
    
    private def convertToMap(dictionary: Dictionary[_, _]): Map[String, Any] = {
        val map = new collection.mutable.HashMap[String, Any]
        import collection.JavaConversions._
        dictionary.keys.foreach { key =>
            val jValue = dictionary.get(key)
            val value =  jValue match {
                case v: Vector[_] => v.toList
                case a: Array[_] => a.toList
                case _ => jValue
            }
            map(key.asInstanceOf[String]) = value.asInstanceOf[Any]
        }
        map.toMap
    }
    
    private class ConfigurationWatcherService(servicePid: String, f: Option[Map[String, Any]] => Unit) extends ManagedService with Module {
            
        var reg: ServiceRegistration = _
        
        var configurationUpdatedModuleContainer: Option[ModuleContainer] = None
        
        def start {
            val propertiesMap = Map(Constants.SERVICE_PID -> servicePid)
            reg = bundleContext.registerService(classOf[ManagedService].getName, this, convertToDictionary(propertiesMap))
        }
        
        def stop {
            configurationUpdatedModuleContainer.foreach(_.stop)
            reg.unregister
            reg = null
        }
        
        def updated(config: Dictionary[_, _]) {
            configurationUpdatedModuleContainer.foreach(_.stop)
            configurationUpdatedModuleContainer = Some(executeWithinNewModuleContainer {
                if (config == null) {
                    f(None)
                } else {
                    f(Some(convertToMap(config)))
                }
            })
        }
    }
}