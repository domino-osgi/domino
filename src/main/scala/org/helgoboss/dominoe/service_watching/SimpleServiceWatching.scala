package org.helgoboss.dominoe.service_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.service_providing.ServiceProviding
import org.helgoboss.dominoe.{DominoeActivator, OsgiContext}

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 10.03.13
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
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
