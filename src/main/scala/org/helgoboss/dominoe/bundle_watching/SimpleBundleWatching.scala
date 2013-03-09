package org.helgoboss.dominoe.bundle_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.OsgiContext

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 10.03.13
 * Time: 00:15
 * To change this template use File | Settings | File Templates.
 */
class SimpleBundleWatching(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext) extends BundleWatching {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}
