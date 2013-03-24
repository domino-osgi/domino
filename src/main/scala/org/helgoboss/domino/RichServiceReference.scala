package org.helgoboss.domino

import org.osgi.framework.{ ServiceReference, BundleContext }

/**
 * Wrapper for a service reference which adds methods to resolve the corresponding service.
 * 
 * @constructor Wraps the given service reference.
 * @param ref Wrapped service reference
 * @param bundleContext Bundle context for resolving the service
 */
class RichServiceReference[S <: AnyRef: ClassManifest](val ref: ServiceReference[S], bundleContext: BundleContext) {
  /**
   * Returns the service for this reference if available.
   */
  def service: Option[S] = Option(bundleContext.getService(ref))

  /**
   * Executes the given function with a service obtained from this reference or with `None`.
   *
   * When the function returns, the service is released using [[org.osgi.framework.BundleContext#ungetService]].
   *
   * @param f function that uses the service
   * @return function result
   */
  def withService[R](f: Option[S] => R): R = {
    val service = Option(bundleContext.getService(ref))
    try f(service) finally bundleContext.ungetService(ref)
  }
}
