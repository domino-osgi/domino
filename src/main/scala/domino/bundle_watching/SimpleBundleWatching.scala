package domino.bundle_watching

import domino.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import domino.OsgiContext

/**
 * A class that mixes in the [[BundleWatching]] trait. Use this if you want to use a class instead of a trait.
 */
class SimpleBundleWatching(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext) extends BundleWatching {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}
