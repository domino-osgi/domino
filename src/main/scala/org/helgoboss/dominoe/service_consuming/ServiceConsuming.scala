package org.helgoboss.dominoe.service_consuming

import org.osgi.framework.{ServiceReference, BundleContext}
import org.helgoboss.dominoe.{DominoeImplicits, DominoeUtil, OsgiContext, RichServiceReference}

/**
 * Provides convenient methods to consume OSGi services.
 */
trait ServiceConsuming extends DominoeImplicits {
  /** Dependency */
  protected def bundleContext: BundleContext

  /**
   * Executes the given function with the highest-ranked service of the specified type. If it's not available,
   * it still executes it but with `None`.
   *
   * When the function returns, the service is released ([[org.osgi.framework.BundleContext#ungetService()]]).
   *
   * @param f function that uses the service
   * @tparam S service type
   * @tparam R function result type
   * @return function result
   */
  def withService[S <: AnyRef: ClassManifest, R](f: Option[S] => R): R = {
    serviceRef[S] match {
      case Some(ref) =>
        val service = bundleContext.getService(ref)
        try f(Some(service)) finally bundleContext.ungetService(ref)

      case None =>
        f(None)
    }
  }

  /**
   * Executes the given function with the first available service of the specified class which satisfies the filter
   * if available. If it's not available, it still executes it but with `None`.
   *
   * When the function returns, the service is released ([[org.osgi.framework.BundleContext#ungetService()]]).
   *
   * @param filter filter expression
   * @param f function that uses the service
   * @tparam S service type
   * @tparam R function result type
   * @return function result
   */
  def withAdvancedService[S <: AnyRef: ClassManifest, R](filter: String)(f: Option[S] => R): R = {
    serviceRef[S](filter) match {
      case Some(ref) =>
        val service = bundleContext.getService(ref)
        try f(Some(service)) finally bundleContext.ungetService(ref)

      case None =>
        f(None)
    }
  }

  /**
   * Returns the highest-ranked service of the specified type if available. The service is not explicitly released
   * (using `ungetService`). It's assumed that the service will be used until the bundle stops.
   *
   * @tparam S service type
   */
  def service[S <: AnyRef: ClassManifest] = {
    serviceRef[S] map { bundleContext.getService(_) }
  }

  /**
   * Like `service`, but returns the reference so you can access meta information about that service.
   * An implicit conversion adds a `service` property to the reference, so you can simply use that to obtain the service.
   */
  def serviceRef[S <: AnyRef: ClassManifest]: Option[ServiceReference[S]] = {
    val className = classManifest[S].erasure.getName
    val ref = bundleContext.getServiceReference(className)
    Option(ref) map { _.asInstanceOf[ServiceReference[S]] }
  }

  /**
   * Returns the first available service of the specified class which satisfies the filter if available. If the service
   * is not available, it returns None. The service is not explicitly released.
   *
   * @param filter filter expression
   *
   * @tparam S service type
   */
  def service[S <: AnyRef: ClassManifest](filter: String) = {
    serviceRef[S](filter) map { bundleContext.getService(_) }
  }

  /**
   * Like `service` with filter, but returns the service reference.
   */
  def serviceRef[S <: AnyRef: ClassManifest](filter: String) = {
    val refs = serviceRefs[S](filter)
    refs.lift(0)
  }

  /**
   * Returns all services of the given type.
   *
   * @tparam S service type
   */
  def services[S <: AnyRef: ClassManifest] = {
    services[S](null.asInstanceOf[String])
  }

  /**
   * Like `services` but returns service references.
   */
  def serviceRefs[S <: AnyRef: ClassManifest]: Seq[ServiceReference[S]] = {
    serviceRefs[S](null.asInstanceOf[String])
  }

  /**
   * Returns all services of the specified type which satisfy the given filter.
   *
   * @param filter filter expression
   *
   * @tparam S service type
   */
  def services[S <: AnyRef: ClassManifest](filter: String) = {
    serviceRefs[S](filter) map { bundleContext.getService(_) }
  }

  /**
   * Like `services` with filters but returns the references.
   */
  def serviceRefs[S <: AnyRef: ClassManifest](filter: String): Seq[ServiceReference[S]] = {
    if (bundleContext == null) {
      Nil
    } else {
      // Build the filter (generic type filter + custom filter)
      val cm = classManifest[S]
      val completeFilter = DominoeUtil.createGenericsAndCustomFilter(cm, filter)

      // Get the list of references matching the filter
      val refs = bundleContext.getServiceReferences(cm.erasure.getName, completeFilter.orNull)

      if (refs == null) {
        Nil
      } else {
        refs.toList.asInstanceOf[Seq[ServiceReference[S]]]
      }
    }
  }

}

