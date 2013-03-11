package org.helgoboss.dominoe.service_providing

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.OsgiContext

/**
 * A class version of the [[org.helgoboss.dominoe.service_providing.ServiceProviding]] trait. Use this if you
 * don't want to use the trait.
 */
class SimpleServiceProviding(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext) extends ServiceProviding {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}
