package org.helgoboss.dominoe

import org.osgi.framework.BundleContext

class SimpleServiceConsumer(protected val bundleContext: BundleContext) extends ServiceConsumer {
  def this(osgiContext: OsgiContext) = this(osgiContext.bundleContext)
}

trait ServiceConsumer {
  protected def bundleContext: BundleContext
  
  def withService[S <: AnyRef: ClassManifest, R](f: Option[S] => R): R = {
    optionalServiceReference[S] match {
      case Some(ref) =>
        try f(Some(ref.service)) finally bundleContext.ungetService(ref)
      
      case None =>
        f(None)
    }
  }

  /**
   * Returns the highest-ranked service of the specified class wrapped in a Some object if available. If the service is not available,
   * it returns None. The service is not explicitly released (using ungetService). It's assumed that the service will be used until
   * the bundle stops.
   */
  def optionalService[S <: AnyRef: ClassManifest] = optionalServiceReference[S] map { _.service }

  /**
   * Like optionalService, but returns the reference so you can access meta information about that service.
   * An implicit conversion adds a "service" property to the reference, so you can simply use that to obtain the service.
   */
  def optionalServiceReference[S <: AnyRef: ClassManifest] = {
    optionalServiceReference[S](null)
  }

  /**
   * Returns the first available service of the specified class and filter wrapped in a Some object if available. If the service is not available,
   * it returns None. The service is not explicitly released.
   */
  def optionalService[S <: AnyRef: ClassManifest](filter: String) = optionalServiceReference[S](filter).map(_.service)

  /**
   * Like optionalService, but returns the service reference.
   */
  def optionalServiceReference[S <: AnyRef: ClassManifest](filter: String) = {
    val refs = serviceReferencesWithFilter[S](filter)
    refs.lift(0)
  }

  def services[S <: AnyRef: ClassManifest] = servicesWithFilter[S](null)

  def serviceReferences[S <: AnyRef: ClassManifest] = serviceReferencesWithFilter[S](null)

  /**
   * Returns all services of the specified class. The services are not explicitly released.
   */
  def services[S <: AnyRef: ClassManifest](filter: String) = servicesWithFilter[S](filter)

  /**
   * Like services, but returns the references.
   */
  def serviceReferences[S <: AnyRef: ClassManifest](filter: String) = serviceReferencesWithFilter[S](filter)

  private def servicesWithFilter[S <: AnyRef: ClassManifest](filter: String): Seq[S] = serviceReferencesWithFilter[S](filter) map { _.service }

  private def serviceReferencesWithFilter[S <: AnyRef: ClassManifest](filter: String): Seq[RichServiceReference[S]] = {
    if (bundleContext == null) {
      Nil
    } else {
      val cm = classManifest[S]
      val completeTypeExpressionFilter = RichService.createCompleteTypeExpressionFilter(cm)
      val completeFilter = RichService.linkFiltersWithAnd(completeTypeExpressionFilter, Option(filter))
      val refs = bundleContext.getServiceReferences(
        cm.erasure.getName,
        completeFilter.orNull)

      if (refs == null) {
        Nil
      } else {
        refs.map { ref =>
          new RichServiceReference[S](ref, bundleContext)
        }
      }
    }
  }

  def requiredService[S <: AnyRef: ClassManifest] = {
    optionalService[S] getOrElse (throw new ServiceNotAvailableException)
  }

  /**
   * Returns the highest-ranked service of the specified class. The case that the service is not available is considered as an
   * exceptional situation, so if the service is not available, an exception is thrown. The service is not explicitly released.
   */
  def requiredService[S <: AnyRef: ClassManifest](filter: String) = {
    optionalService[S](filter) getOrElse (throw new ServiceNotAvailableException)
  }

}