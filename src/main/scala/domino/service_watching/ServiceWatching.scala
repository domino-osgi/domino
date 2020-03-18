package domino.service_watching

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

import domino.{DominoImplicits, DominoUtil}
import domino.capsule.{Capsule, CapsuleContext, CapsuleScope}
import domino.service_watching.monitor.{ServiceWatcher, ServiceWatchingMonitor}
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker

/**
 * Provides convenient methods to add a service watcher to the current scope or wait until services are present.
 *
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
  def watchServices[S <: AnyRef : TypeTag : ClassTag](f: ServiceWatcherEvent[S] => Unit): ServiceTracker[S, S] = {
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
  def watchAdvancedServices[S <: AnyRef : TypeTag : ClassTag](filter: String)(f: ServiceWatcherEvent[S] => Unit): ServiceTracker[S, S] = {
    val combinedFilter = DominoUtil.createCompleteFilter(typeTag[S].tpe, filter)
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
  def whenServicePresent[S <: AnyRef : TypeTag : ClassTag](f: (S) => Unit): ServiceTracker[S, S] = {
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
  def whenAdvancedServicePresent[S <: AnyRef : TypeTag : ClassTag](filter: String)(f: S => Unit): ServiceTracker[S, S] = {
    case class ActivationState(watchedService: S, servicePresentCapsuleScope: CapsuleScope)
    var optActivationState: Option[ActivationState] = None

    Option(bundleContext.getServiceReference(classOf[ServiceWatchingMonitor]))
      .map(bundleContext.getService(_))
      .foreach { ms =>
        val watcher = new ServiceWatcher {
          override val target: String = DominoUtil.createCompleteFilter(typeTag[S].tpe, filter)
          override def isSatisfied: Boolean = optActivationState.isDefined
        }
        ms.registerWatcher(watcher)
        capsuleContext.addCapsule(new Capsule {
          override def start(): Unit = {}
          override def stop(): Unit = {
            ms.unregisterWatcher(watcher)
          }
        })
      }


    watchAdvancedServices[S](filter) {
      case ServiceWatcherEvent.AddingService(s, context) =>
        if (optActivationState.isEmpty) {
          // Not already watching a service of this type. Run handler.
          val c = capsuleContext.executeWithinNewCapsuleScope {
            //            log.debug(s"Bundle ${DominoUtil.dumpBundle(bundleContext)}: Service of type ${classTag[S].runtimeClass} became available: ${s}")
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
  def whenServicesPresent[S1 <: AnyRef : TypeTag : ClassTag, S2 <: AnyRef : TypeTag : ClassTag](f: (S1, S2) => Unit): ServiceTracker[S1, S1] = {

    whenServicePresent[S1] { (service1: S1) =>
      whenServicePresent[S2] { (service2: S2) =>
        f(service1, service2)
      }
    }
  }

  /**
   * @group WaitForServices
   */
  def whenServicesPresent[S1 <: AnyRef : TypeTag : ClassTag, S2 <: AnyRef : TypeTag : ClassTag, S3 <: AnyRef : TypeTag : ClassTag](f: (S1, S2, S3) => Unit): ServiceTracker[S1, S1] = {

    whenServicesPresent[S1, S2] { (service1: S1, service2: S2) =>
      whenServicePresent[S3] { (service3: S3) =>
        f(service1, service2, service3)
      }
    }
  }

  /**
   * @group WaitForServices
   */
  def whenServicesPresent[S1 <: AnyRef : TypeTag : ClassTag, S2 <: AnyRef : TypeTag : ClassTag, S3 <: AnyRef : TypeTag : ClassTag, S4 <: AnyRef : TypeTag : ClassTag](f: (S1, S2, S3, S4) => Unit): ServiceTracker[S1, S1] = {

    whenServicesPresent[S1, S2, S3] { (service1: S1, service2: S2, service3: S3) =>
      whenServicePresent[S4] { (service4: S4) =>
        f(service1, service2, service3, service4)
      }
    }
  }

  /**
   * @group WaitForServices
   */
  def whenServicesPresent[S1 <: AnyRef : TypeTag : ClassTag, S2 <: AnyRef : TypeTag : ClassTag, S3 <: AnyRef : TypeTag : ClassTag, S4 <: AnyRef : TypeTag : ClassTag, S5 <: AnyRef : TypeTag : ClassTag](f: (S1, S2, S3, S4, S5) => Unit): ServiceTracker[S1, S1] = {

    whenServicesPresent[S1, S2, S3, S4] { (service1: S1, service2: S2, service3: S3, service4: S4) =>
      whenServicePresent[S5] { (service5: S5) =>
        f(service1, service2, service3, service4, service5)
      }
    }
  }
}

