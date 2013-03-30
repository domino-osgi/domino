package org.helgoboss.domino

import org.osgi.framework.{BundleContext, ServiceReference}

/**
 * Provides common implicit conversions. This is used by the other traits.
 */
trait DominoImplicits {
  /** Dependency */
  protected def bundleContext: BundleContext

  /**
   * Converts a service reference to a rich service reference so one can easily obtain the
   * corresponding service by calling `service`, for example.
   */
  implicit def serviceRefToRichServiceRef[S <: AnyRef](serviceRef: ServiceReference[S]) = {
    new RichServiceReference(serviceRef, bundleContext)
  }
}
