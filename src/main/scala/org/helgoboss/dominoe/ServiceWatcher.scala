package org.helgoboss.dominoe

import org.osgi.util.tracker.ServiceTracker
import org.helgoboss.module_support._
import org.helgoboss.status_logger.Status
import org.osgi.framework._

class SimpleServiceWatcher(
    protected val moduleContext: ModuleContext,
    protected val bundleContext: BundleContext,
    protected val serviceProvider: ServiceProvider) extends ServiceWatcher {

  def this(osgiContext: OsgiContext, serviceProvider: ServiceProvider) = this(
    osgiContext,
    osgiContext.bundleContext,
    serviceProvider)

  def this(DominoeActivator: DominoeActivator) = this(
    DominoeActivator,
    DominoeActivator)
}

trait ServiceWatcher {
  protected val serviceProvider: ServiceProvider
  protected def moduleContext: ModuleContext
  protected def bundleContext: BundleContext

  import serviceProvider._

  private class ServiceWatcher[S <: AnyRef: ClassManifest](mf: ClassManifest[_], f: ServiceWatcherEvent[S] => Unit) extends Module {
    var tracker: ServiceTracker = _

    def start() {
      /* Construct filter containing generic type info if necessary */
      val objectClassFilterString = Some("(" + Constants.OBJECTCLASS + "=" + mf.erasure.getName + ")")
      val completeTypesExpressionFilterString = RichService.createCompleteTypeExpressionFilter(mf)

      val completeFilterString = RichService.linkFiltersWithAnd(
        objectClassFilterString,
        completeTypesExpressionFilterString)

      val completeFilter = bundleContext.createFilter(completeFilterString.get)

      /* Create tracker matching this filter */
      tracker = new ServiceTracker(bundleContext, completeFilter, null) {
        override def addingService(ref: ServiceReference) = {
          val service = context getService ref
          val watcherEvent = AddingService(service.asInstanceOf[S], ServiceWatcherContext(tracker, new RichServiceReference[S](ref, bundleContext)))
          f(watcherEvent)
          service
        }

        override def modifiedService(ref: ServiceReference, service: AnyRef) {
          val watcherEvent = ModifiedService(service.asInstanceOf[S], ServiceWatcherContext(tracker, new RichServiceReference[S](ref, bundleContext)))
          f(watcherEvent)
        }

        override def removedService(ref: ServiceReference, service: AnyRef) {
          val watcherEvent = RemovedService(service.asInstanceOf[S], ServiceWatcherContext(tracker, new RichServiceReference[S](ref, bundleContext)))
          f(watcherEvent)
          context ungetService ref
        }
      }
      tracker.open()
    }

    def stop() {
      tracker.close()
      tracker = null
    }
  }

  /**
   * Watches the coming, going and changing of services of the given type and allows you to react on it.
   */
  def watchServices[S <: AnyRef: ClassManifest](f: ServiceWatcherEvent[S] => Unit): ServiceTracker = {
    val sw = new ServiceWatcher[S](classManifest[S], f)
    moduleContext.addModule(sw)
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
   * TODO Fallback to another service if s is removed and another service of the type is available (reevaluate module).
   */
  def whenFilteredServicePresent[S <: AnyRef: ClassManifest](filter: ServiceWatcherContext[S] => Boolean)(f: (S) => Unit): ServiceTracker = {
  
    case class ActivationState(watchedService: S, servicePresentModuleContainer: ModuleContainer)
    var optActivationState: Option[ActivationState] = None

    val status = Status("waiting for service [" + classManifest[S] + "]")

    var statusServiceReg: Option[ServiceRegistration] = None

    def registerStatus() {
      // TODO implement correctly
      // statusServiceReg = Some(status.providesService[Status])
    }
    def unregisterStatus() {
      // TODO implement correctly
      // statusServiceReg foreach { _.unregister() }
      // statusServiceReg = None
    }

    registerStatus()

    watchServices[S] {
      case AddingService(s, context) =>
        if (filter(context) && optActivationState.isEmpty) {
          /* Not already watching a service of this type */
          val c = moduleContext.executeWithinNewModuleContainer {
            f(s)
          }
          optActivationState = Some(ActivationState(watchedService = s, servicePresentModuleContainer = c))
          unregisterStatus()
        }

      case RemovedService(s, context) =>
        optActivationState foreach { activationState =>
          if (s == activationState.watchedService) {
            activationState.servicePresentModuleContainer.stop()
            optActivationState = None
            if (bundleContext.getBundle.getState == Bundle.ACTIVE) {
              registerStatus()
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