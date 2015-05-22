package domino.service_watching

import domino.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import domino.service_providing.ServiceProviding
import domino.{DominoActivator, OsgiContext}

/**
 * A class that mixes in the [[ServiceWatching]] trait. Use this if you want to use a class instead of a trait.
 */
class SimpleServiceWatching(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext,
    protected val serviceProviding: ServiceProviding) extends ServiceWatching {

  def this(osgiContext: OsgiContext, serviceProviding: ServiceProviding) = this(
    osgiContext,
    osgiContext.bundleContext,
    serviceProviding)

  def this(DominoActivator: DominoActivator) = this(
    DominoActivator,
    DominoActivator)
}
