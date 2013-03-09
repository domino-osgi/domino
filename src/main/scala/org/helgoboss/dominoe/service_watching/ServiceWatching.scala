package org.helgoboss.dominoe.service_watching

import org.helgoboss.dominoe.service_providing.ServiceProviding
import org.helgoboss.capsule.{CapsuleContainer, CapsuleContext}
import org.osgi.framework.{Bundle, ServiceRegistration, BundleContext}
import org.osgi.util.tracker.ServiceTracker
import org.helgoboss.dominoe.{DominoeActivator, OsgiContext}

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 09.03.13
 * Time: 22:26
 * To change this template use File | Settings | File Templates.
 */
trait ServiceWatching {
  protected val serviceProviding: ServiceProviding
  protected def capsuleContext: CapsuleContext
  protected def bundleContext: BundleContext

  import serviceProviding._



  /**
   * Watches the coming, going and changing of services of the given type and allows you to react on it.
   */
  def watchServices[S <: AnyRef: ClassManifest](f: ServiceWatcherEvent[S] => Unit): ServiceTracker = {
    val sw = new ServiceWatcherCapsule[S](classManifest[S], f, bundleContext)
    capsuleContext.addCapsule(sw)
    sw.tracker
  }

  def whenServicePresent[S <: AnyRef: ClassManifest](f: (S) => Unit): ServiceTracker = {
    whenFilteredServicePresent[S](_ => true)(f)
  }

  /**
   * Activates the given inner logic as long as the first service of the given type is present. This implements the concept of
   * required services. The inner logic is started as soon as a service s of the given type gets present and stopped when s is removed.
   * You can wait on a bunch of services if you nest several whenServicePresent methods.
   *
   * TODO Fallback to another service if s is removed and another service of the type is available (reevaluate capsule).
   */
  def whenFilteredServicePresent[S <: AnyRef: ClassManifest](filter: ServiceWatcherContext[S] => Boolean)(f: (S) => Unit): ServiceTracker = {

    case class ActivationState(watchedService: S, servicePresentCapsuleContainer: CapsuleContainer)
    var optActivationState: Option[ActivationState] = None

    watchServices[S] {
      case AddingService(s, context) =>
        if (filter(context) && optActivationState.isEmpty) {
          /* Not already watching a service of this type */
          val c = capsuleContext.executeWithinNewCapsuleContainer {
            f(s)
          }
          optActivationState = Some(ActivationState(watchedService = s, servicePresentCapsuleContainer = c))
        }

      case RemovedService(s, context) =>
        optActivationState foreach { activationState =>
          if (s == activationState.watchedService) {
            activationState.servicePresentCapsuleContainer.stop()
            optActivationState = None
            if (bundleContext.getBundle.getState == Bundle.ACTIVE) {
            }
          }
        }

      case _ =>
    }
  }

  def whenServicesPresent[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest](f: (S1, S2) => Unit): ServiceTracker = {
    whenServicePresent[S1] { (service1: S1) =>
      whenServicePresent[S2] { (service2: S2) =>
        f(service1, service2)
      }
    }
  }

  def whenServicesPresent[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest](f: (S1, S2, S3) => Unit): ServiceTracker = {
    whenServicesPresent[S1, S2] { (service1: S1, service2: S2) =>
      whenServicePresent[S3] { (service3: S3) =>
        f(service1, service2, service3)
      }
    }
  }

  def whenServicesPresent[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest, S4 <: AnyRef: ClassManifest](f: (S1, S2, S3, S4) => Unit): ServiceTracker = {
    whenServicesPresent[S1, S2, S3] { (service1: S1, service2: S2, service3: S3) =>
      whenServicePresent[S4] { (service4: S4) =>
        f(service1, service2, service3, service4)
      }
    }
  }

  def whenServicesPresent[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest, S4 <: AnyRef: ClassManifest, S5 <: AnyRef: ClassManifest](f: (S1, S2, S3, S4, S5) => Unit): ServiceTracker = {
    whenServicesPresent[S1, S2, S3, S4] { (service1: S1, service2: S2, service3: S3, service4: S4) =>
      whenServicePresent[S5] { (service5: S5) =>
        f(service1, service2, service3, service4, service5)
      }
    }
  }
}

