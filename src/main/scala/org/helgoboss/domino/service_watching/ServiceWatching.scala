package org.helgoboss.domino.service_watching

import org.helgoboss.domino.service_providing.ServiceProviding
import org.helgoboss.capsule.{CapsuleScope, CapsuleContext}
import org.osgi.framework.{Bundle, ServiceRegistration, BundleContext}
import org.osgi.util.tracker.ServiceTracker
import org.helgoboss.domino.{DominoImplicits, DominoUtil, DominoActivator, OsgiContext}

/**
 * Provides convenient methods to add a service watcher to the current scope or wait until services are present.
 
 * @groupname WatchServices Watch services
 * @groupdesc WatchServices Methods for reacting to service events
 * @groupname WaitForServices Wait for services
 * @groupdesc WaitForServices Methods for waiting until services become available
 */
trait ServiceWatching extends DominoImplicits {
  /** Dependency */
  protected def capsuleContext: CapsuleContext

  /** Dependency */
  protected def bundleContext: BundleContext


  /**
   * Lets you react to service events for services with the specified type.
   *
   * @group WatchServices
   * @param f Service event handler
   * @tparam S Service type
   * @return Underlying service tracker
   */
  def watchServices[S <: AnyRef: ClassManifest](f: ServiceWatcherEvent[S] => Unit): ServiceTracker[S, S] = {
    watchAdvancedServices[S](null)(f)
  }

  /**
   * Lets you react to service events for services with the specified type which match the given filter.
   *
   * @group WatchServices
   * @param f Service event handler
   * @tparam S Service type
   * @return Underlying service tracker
   */
  def watchAdvancedServices[S <: AnyRef: ClassManifest](filter: String)(f: ServiceWatcherEvent[S] => Unit): ServiceTracker[S, S] = {
    val combinedFilter = DominoUtil.createCompleteFilter(classManifest[S], filter)
    val typedFilter = bundleContext.createFilter(combinedFilter)
    val sw = new ServiceWatcherCapsule[S](typedFilter, f, bundleContext)
    capsuleContext.addCapsule(sw)
    sw.tracker
  }

  /**
   * Waits until a service of the specified type is available and executes the given event handler with it.
   * When the service disappears, the capsules added in the handlers are stopped.
   * You can wait on a bunch of services if you nest `whenServicePresent` methods.
   *
   * @group WaitForServices
   * @param f Handler
   * @tparam S Service type
   * @return Underlying service tracker
   */
  def whenServicePresent[S <: AnyRef: ClassManifest](f: (S) => Unit): ServiceTracker[S, S] = {
    whenAdvancedServicePresent[S](null)(f)
  }

  /**
   * Activates the given inner logic as long as the first service of the given type is present. This implements the concept of
   * required services. The inner logic is started as soon as a service s of the given type gets present and stopped when s is removed.
   *
   * @group WaitForServices
   * @todo Idea for roadmap: Fallback to another service if s is removed and another service of the type is available 
   *       (reevaluate capsule).
   */
  def whenAdvancedServicePresent[S <: AnyRef: ClassManifest](filter: String)(f: S => Unit): ServiceTracker[S, S] = {
    case class ActivationState(watchedService: S, servicePresentCapsuleScope: CapsuleScope)
    var optActivationState: Option[ActivationState] = None

    watchAdvancedServices[S](filter) {
      case ServiceWatcherEvent.AddingService(s, context) =>
        if (optActivationState.isEmpty) {
          // Not already watching a service of this type. Run handler.
          val c = capsuleContext.executeWithinNewCapsuleScope {
            f(s)
          }

          // Save the activation state
          optActivationState = Some(ActivationState(watchedService = s, servicePresentCapsuleScope = c))
        }

      case ServiceWatcherEvent.RemovedService(s, context) =>
        optActivationState foreach { activationState =>
          // Stop the capsule scope only if exactly that service got removed which triggered its creation
          if (s == activationState.watchedService) {
            activationState.servicePresentCapsuleScope.stop()
            optActivationState = None
          }
        }

      case _ =>
    }
  }

  /**
   * @group WaitForServices
   */
  def whenServicesPresent[
      S1 <: AnyRef: ClassManifest,
      S2 <: AnyRef: ClassManifest](f: (S1, S2) => Unit): ServiceTracker[S1, S1] = {

    whenServicePresent[S1] { (service1: S1) =>
      whenServicePresent[S2] { (service2: S2) =>
        f(service1, service2)
      }
    }
  }

  /**
   * @group WaitForServices
   */
  def whenServicesPresent[
      S1 <: AnyRef: ClassManifest,
      S2 <: AnyRef: ClassManifest,
      S3 <: AnyRef: ClassManifest](f: (S1, S2, S3) => Unit): ServiceTracker[S1, S1] = {

    whenServicesPresent[S1, S2] { (service1: S1, service2: S2) =>
      whenServicePresent[S3] { (service3: S3) =>
        f(service1, service2, service3)
      }
    }
  }

  /**
   * @group WaitForServices
   */
  def whenServicesPresent[
      S1 <: AnyRef: ClassManifest,
      S2 <: AnyRef: ClassManifest,
      S3 <: AnyRef: ClassManifest,
      S4 <: AnyRef: ClassManifest](f: (S1, S2, S3, S4) => Unit): ServiceTracker[S1, S1] = {

    whenServicesPresent[S1, S2, S3] { (service1: S1, service2: S2, service3: S3) =>
      whenServicePresent[S4] { (service4: S4) =>
        f(service1, service2, service3, service4)
      }
    }
  }

  /**
   * @group WaitForServices
   */
  def whenServicesPresent[
      S1 <: AnyRef: ClassManifest,
      S2 <: AnyRef: ClassManifest,
      S3 <: AnyRef: ClassManifest,
      S4 <: AnyRef: ClassManifest,
      S5 <: AnyRef: ClassManifest](f: (S1, S2, S3, S4, S5) => Unit): ServiceTracker[S1, S1] = {

    whenServicesPresent[S1, S2, S3, S4] { (service1: S1, service2: S2, service3: S3, service4: S4) =>
      whenServicePresent[S5] { (service5: S5) =>
        f(service1, service2, service3, service4, service5)
      }
    }
  }
}

