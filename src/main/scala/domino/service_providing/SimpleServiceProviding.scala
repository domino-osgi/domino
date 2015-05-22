package domino.service_providing

import domino.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import domino.OsgiContext

/**
 * A class that mixes in the [[ServiceProviding]] trait. Use this if you want to use a class instead of a trait.
 */
class SimpleServiceProviding(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext) extends ServiceProviding {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}
