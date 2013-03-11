package org.helgoboss.dominoe

import org.osgi.framework.{BundleContext, ServiceReference}

/**
 * Common implicit conversions. This is used by the other traits.
 */
trait DominoeImplicits {
  protected def bundleContext: BundleContext

  /**
   * Converts a service reference to a rich service reference so one can easily obtain the
   * corresponding service by calling `service`, for example.
   */
  implicit def serviceRefToRichServiceRef[S <: AnyRef: ClassManifest](serviceRef: ServiceReference[S]) = {
    new RichServiceReference(serviceRef, bundleContext)
  }
}
