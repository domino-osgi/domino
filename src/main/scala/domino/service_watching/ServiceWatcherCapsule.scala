package domino.service_watching

import domino.capsule.Capsule
import org.osgi.framework.{BundleContext, Filter, ServiceReference}
import org.osgi.util.tracker.ServiceTracker
import java.util.logging.{Logger => JLogger}

import domino.scala_logging.JavaUtilLoggingLogger
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

/**
 * A capsule which executes the given event handlers on service state transitions while the current scope is active.
 * Tracks all state transitions and services visible to the class loader. The custom object facility of the service
 * tracker is not used.
 *
 * @param filter Filter expression restricting the set of services to be tracked
 * @param f Event handlers
 * @param bundleContext Bundle context
 * @tparam S Service type to be tracked
 */
class ServiceWatcherCapsule[S <: AnyRef](
    filter: Filter,
    f: ServiceWatcherEvent[S] => Unit,
    bundleContext: BundleContext)(implicit tag: ClassTag[S]) extends Capsule {

  protected var _tracker: ServiceTracker[S, S] = _

  private[this] val log = LoggerFactory.getLogger(getClass.getName)
  private[this] val svcClassName = tag.runtimeClass.getName
  private[this] val filterString = filter.toString

  /**
   * Returns the underlying service tracker as long as the current capsule scope is active.
   */
  def tracker = _tracker

  def start() {
    log.debug(logEvent("Starting tracker"))

    // Create tracker matching this filter
    _tracker = new ServiceTracker[S, S](bundleContext, filter, null) {
      override def addingService(ref: ServiceReference[S]) = {
        log.debug(logEvent("Added service"))
        val service = context.getService(ref)
        val watcherEvent = ServiceWatcherEvent.AddingService(service, ServiceWatcherContext(_tracker, ref))
        f(watcherEvent)
        service
      }

      override def modifiedService(ref: ServiceReference[S], service: S) {
        log.debug(logEvent("Modified service"))
        val watcherEvent = ServiceWatcherEvent.ModifiedService(service, ServiceWatcherContext(_tracker, ref))
        f(watcherEvent)
      }

      override def removedService(ref: ServiceReference[S], service: S) {
        log.debug(logEvent("Removed service"))
        val watcherEvent = ServiceWatcherEvent.RemovedService(service, ServiceWatcherContext(_tracker, ref))
        try f(watcherEvent) finally context.ungetService(ref)
      }
    }

    // Open tracker
    _tracker.open()
  }

  def stop() {
    log.debug(logEvent("Stopping tracker"))
    // Close tracker
    _tracker.close()
    _tracker = null
  }

  private[this] def logEvent(prefix: String) : String = {
    s"$prefix for service class [$svcClassName] and filter[$filterString]"
  }
}