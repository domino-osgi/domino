package org.helgoboss.dominoe.service_providing

import org.osgi.framework.BundleContext
import org.helgoboss.capsule.CapsuleContext
import org.helgoboss.dominoe.OsgiContext

/**
 * Provides an implicit conversion to make any object easily registerable.
 */
trait ServiceProviding {
  /** Dependency */
  protected def bundleContext: BundleContext

  /** Dependency */
  protected def capsuleContext: CapsuleContext

  /**
   * Automatically converts any object to a [[org.helgoboss.dominoe.service_providing.ProvidableService]].
   */
  implicit def serviceToProvidableService[S](service: S) = {
    new ProvidableService[S](service, capsuleContext, bundleContext)
  }
}

