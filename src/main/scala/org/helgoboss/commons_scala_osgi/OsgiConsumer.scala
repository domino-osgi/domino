package org.helgoboss.commons_scala_osgi

import org.osgi.framework.{BundleContext, BundleActivator, ServiceReference}
import java.lang.reflect.{InvocationHandler, Proxy, Method}

trait OsgiConsumer extends OsgiContext {

    /**
     * Returns the highest-ranked service of the specified class wrapped in a Some object if available. If the service is not available,
     * it returns None. The service is not explicitly released (using ungetService). It's assumed that the service will be used until
     * the bundle stops.
     */
    protected def optionalService[S <: AnyRef: ClassManifest] = optionalServiceReference[S].map(_.service)
    
    /**
     * Like optionalService, but returns the reference so you can access meta information about that service. An implicit conversion adds
     * a "service" property to the reference, so you can simply use that to obtain the service.
     */
    protected def optionalServiceReference[S <: AnyRef: ClassManifest] = {
        val ref = bundleContext.getServiceReference(classManifest[S].erasure.getName)
        if (ref == null) {
            None
        } else {
            Some(new RichServiceReference[S](ref, bundleContext))
        }
    }
    
    /**
     * Returns the first available service of the specified class and filter wrapped in a Some object if available. If the service is not available,
     * it returns None. The service is not explicitly released.
     */
    protected def optionalService[S <: AnyRef: ClassManifest](filter: String) = optionalServiceReference[S](filter).map(_.service)
    
    /**
     * Like optionalService, but returns the service reference.
     */
    protected def optionalServiceReference[S <: AnyRef: ClassManifest](filter: String) = {
        val refs = bundleContext.getServiceReferences(classManifest[S].erasure.getName, filter)
        if (refs == null) {
            None
        } else {
            refs.lift(0) match {
                case Some(ref) =>
                    Some(new RichServiceReference[S](ref, bundleContext))
                case None =>
                    None
            }
        }
    }
    
    
    
    
    
    protected def services[S <: AnyRef: ClassManifest] = servicesWithFilter[S](null)
    
    protected def serviceReferences[S <: AnyRef: ClassManifest] = serviceReferencesWithFilter[S](null)

    
    /**
     * Returns all services of the specified class. The services are not explicitly released.
     */
    protected def services[S <: AnyRef: ClassManifest](filter: String) = servicesWithFilter[S](filter)
    
    /**
     * Like services, but returns the references.
     */
    protected def serviceReferences[S <: AnyRef: ClassManifest](filter: String) = serviceReferencesWithFilter[S](filter)
    
    
    
    private def servicesWithFilter[S <: AnyRef: ClassManifest](filter: String): Seq[S] = serviceReferencesWithFilter[S](filter).map(_.service)
    
    private def serviceReferencesWithFilter[S <: AnyRef: ClassManifest](filter: String): Seq[RichServiceReference[S]] = {
        val refs = bundleContext.getServiceReferences(classManifest[S].erasure.getName, filter)
        if (refs == null) {
            Nil
        } else {
            refs.map { ref =>
                new RichServiceReference[S](ref, bundleContext)
            }
        }
    }
    
    protected def requiredService[S <: AnyRef: ClassManifest] = {
        optionalService[S] getOrElse (throw new ServiceNotAvailableException)
    }
    
    /**
     * Returns the highest-ranked service of the specified class. The case that the service is not available is considered as an
     * exceptional situation, so if the service is not available, an exception is thrown. The service is not explicitly released.
     */
    protected def requiredService[S <: AnyRef: ClassManifest](filter: String) = {
        optionalService[S](filter) getOrElse (throw new ServiceNotAvailableException)
    }
    
}