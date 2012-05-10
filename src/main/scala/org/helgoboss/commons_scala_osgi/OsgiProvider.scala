package org.helgoboss.commons_scala_osgi

import org.osgi.framework.{BundleContext, BundleActivator, ServiceRegistration}
import org.helgoboss.module_support.Module

trait OsgiProvider extends OsgiContext {
    protected implicit def service2RichService(service: AnyRef) = new RichService(service)
    
    protected class RichService(service: Any) {
        def providesService[S <: AnyRef: ClassManifest](properties: (String, Any)*): ServiceRegistration = {
            providesServiceInternal(interfaces = List(classManifest[S].erasure), properties = properties)
        }
        
        def providesService[S <: AnyRef: ClassManifest]: ServiceRegistration = {
            providesService[S]()
        }
                
        def providesService[S <: AnyRef: ClassManifest](properties: Map[String, Any]): ServiceRegistration = {
            providesService[S](properties.toSeq :_*)
        }
        
        def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest](properties: (String, Any)*): ServiceRegistration = {
            providesServiceInternal(interfaces = List(classManifest[S1].erasure, classManifest[S2].erasure), properties = properties)
        }
        
        def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest]: ServiceRegistration = {
            providesService[S1, S2]()
        }
                
        def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest](properties: Map[String, Any]): ServiceRegistration = {
            providesService[S1, S2](properties.toSeq :_*)
        }       
        
        def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest](properties: (String, Any)*): ServiceRegistration = {
            providesServiceInternal(interfaces = List(classManifest[S1].erasure, classManifest[S2].erasure, classManifest[S3].erasure), properties = properties)
        }
        
        def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest]: ServiceRegistration = {
            providesService[S1, S2, S3]()
        }
                
        def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest](properties: Map[String, Any]): ServiceRegistration = {
            providesService[S1, S2, S3](properties.toSeq :_*)
        }        
        
        
        private def providesServiceInternal(interfaces: List[Class[_]], properties: Seq[(String, Any)]): ServiceRegistration = {
            val sp = new ServiceProvider(interfaces = interfaces, properties = properties)
            addModule(sp)
            sp.reg
        }
        
        private class ServiceProvider(interfaces: List[Class[_]], properties: Seq[(String, Any)]) extends Module {
            var reg: ServiceRegistration = _
        
            def start {
                val javaPropertiesHashtable = new java.util.Hashtable[String, AnyRef] 
                properties.foreach { tuple =>
                    javaPropertiesHashtable.put(tuple._1, tuple._2.asInstanceOf[AnyRef])
                }
                
                reg = bundleContext.registerService(interfaces.map(_.getName).toArray, service, javaPropertiesHashtable)
            }
            
            def stop {
                try {
                    reg.unregister()
                } catch {
                    case x: IllegalStateException =>
                        // Do nothing. Was already unregistered.
                }
                reg = null
            }
        }
    }
}