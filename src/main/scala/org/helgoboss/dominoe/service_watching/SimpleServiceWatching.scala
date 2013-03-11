package org.helgoboss.dominoe.service_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.service_providing.ServiceProviding
import org.helgoboss.dominoe.{DominoeActivator, OsgiContext}

/**
 * A class version of the [[org.helgoboss.dominoe.service_watching.ServiceWatching]] trait. Use this if you
 * don't want to use the trait.
 */
class SimpleServiceWatching(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext,
    protected val serviceProviding: ServiceProviding) extends ServiceWatching {

  def this(osgiContext: OsgiContext, serviceProviding: ServiceProviding) = this(
    osgiContext,
    osgiContext.bundleContext,
    serviceProviding)

  def this(DominoeActivator: DominoeActivator) = this(
    DominoeActivator,
    DominoeActivator)
}
