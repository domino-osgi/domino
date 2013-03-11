package org.helgoboss.dominoe.bundle_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.OsgiContext

/**
 * A class version of the [[org.helgoboss.dominoe.bundle_watching.BundleWatching]] trait. Use this if you don't want
 * to use the trait.
 */
class SimpleBundleWatching(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext) extends BundleWatching {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}
