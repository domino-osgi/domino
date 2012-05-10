package org.helgoboss.commons_scala_osgi

import org.osgi.util.tracker.ServiceTracker
import org.helgoboss.module_support.{Module, ModuleContainer}
import org.helgoboss.status_logger.Status
import org.osgi.framework._

trait OsgiServiceWatcher extends OsgiContext with OsgiProvider {
    protected abstract sealed class ServiceWatcherEvent[S <: AnyRef](service: S, context: ServiceWatcherContext[S])
    protected case class AddingService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)
    protected case class ModifiedService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)
    protected case class RemovedService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)
    
    protected case class ServiceWatcherContext[S <: AnyRef](tracker: ServiceTracker, reference: RichServiceReference[S])
    
    protected class ServiceWatcher[S <: AnyRef: ClassManifest](interface: Class[_], f: ServiceWatcherEvent[S] => Unit) extends Module {
        var tracker: ServiceTracker = _
    
        def start {
            tracker = new ServiceTracker(bundleContext, interface.getName, null) {
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
            tracker.open
        }
        
        def stop {
            tracker.close
            tracker = null
        }
    }
    
    /**
     * Watches the coming, going and changing of services of the given type and allows you to react on it.
     */
    protected def watchServices[S <: AnyRef: ClassManifest](f: ServiceWatcherEvent[S] => Unit): ServiceTracker = {
        val sw = new ServiceWatcher[S](classManifest[S].erasure, f)
        addModule(sw)
        sw.tracker
    }
        
    /**
     * Activates the given inner logic as long as the first service of the given type is present. This implements the concept of 
     * required services. The inner logic is started as soon as a service s of the given type gets present and stopped when s is removed. 
     * You can wait on a bunch of services if you nest several whenServicePresent methods.
     *
     * TODO Fallback to another service if s is removed and another service of the type is available (reevaluate togglable).
     */
    protected def whenServicePresent[S <: AnyRef: ClassManifest](f: (S) => Unit): ServiceTracker = {
        case class ActivationState(watchedService: S, servicePresentModuleContainer: ModuleContainer)
        var optActivationState: Option[ActivationState] = None

        val status = Status("waiting for service [" + classManifest[S].erasure.getName + "]")

        var statusServiceReg: Option[ServiceRegistration] = None

        def registerStatus() {
            statusServiceReg = Some(status.providesService[Status])
        }
        def unregisterStatus() {
            statusServiceReg.foreach(_.unregister())
            statusServiceReg = None
        }

        registerStatus()

        watchServices[S] {
            case AddingService(s, context) => 
                if (optActivationState.isEmpty) {
                    /* Not already watching a service of this type */
                    val c = executeWithinNewModuleContainer {
                        f(s)
                    }
                    optActivationState = Some(ActivationState(watchedService = s, servicePresentModuleContainer = c))
                    unregisterStatus()
                }
                
            case RemovedService(s, context) =>
                optActivationState.foreach { activationState =>
                    if (s == activationState.watchedService) {
                        activationState.servicePresentModuleContainer.stop
                        optActivationState = None
                        if (bundleContext.getBundle.getState == Bundle.ACTIVE) {
                            registerStatus()
                        }
                    }
                }
            
            case _ =>
        }
    }
    
    protected def whenServicesPresent[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest](f: (S1, S2) => Unit): ServiceTracker = {
        whenServicePresent[S1] { (service1: S1) =>
            whenServicePresent[S2] { (service2: S2) =>
                f(service1, service2)
            }
        }
    }
    
    protected def whenServicesPresent[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest](f: (S1, S2, S3) => Unit): ServiceTracker = {
        whenServicesPresent[S1, S2] { (service1: S1, service2: S2) =>
            whenServicePresent[S3] { (service3: S3) =>
                f(service1, service2, service3)
            }
        }
    }
}