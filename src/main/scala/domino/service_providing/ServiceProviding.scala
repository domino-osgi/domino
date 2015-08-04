package domino.service_providing

import scala.language.implicitConversions
import org.osgi.framework.BundleContext
import domino.capsule.CapsuleContext

/**
 * Provides an implicit conversion to make any object easily exposable in the OSGi service registry.
 *
 * @groupname ProvideServices Provide services
 * @groupdesc ProvideServices Functionality for exposing arbitrary objects in the OSGi service registry.
 */
trait ServiceProviding {
  /** Dependency */
  protected def bundleContext: BundleContext

  /** Dependency */
  protected def capsuleContext: CapsuleContext

  /**
   * Automatically converts any object to a [[ProvidableService]].
   *
   * @group ProvideServices
   * @see [[ProvidableService]] for the list of service registration methods
   */
  implicit def serviceToProvidableService[S](service: S) = {
    new ProvidableService[S](service, capsuleContext, bundleContext)
  }
}

