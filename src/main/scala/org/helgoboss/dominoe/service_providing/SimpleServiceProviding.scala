package org.helgoboss.dominoe.service_providing

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.OsgiContext

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 10.03.13
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
class SimpleServiceProviding(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext) extends ServiceProviding {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}
