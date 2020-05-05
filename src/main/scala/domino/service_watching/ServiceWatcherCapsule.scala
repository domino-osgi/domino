package domino.service_watching

import domino.DominoUtil
import domino.capsule.Capsule
import domino.logging.internal.DominoLogger
import org.osgi.framework.{ BundleContext, Filter, ServiceReference }
import org.osgi.util.tracker.ServiceTracker

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
  bundleContext: BundleContext
)
  extends Capsule {

  private[this] var _tracker: ServiceTracker[S, S] = _

  private[this] val log = DominoLogger[this.type]

  /**
   * Returns the underlying service tracker as long as the current capsule scope is active.
   */
  def tracker = _tracker

  def start() {
    log.debug(s"Bundle ${DominoUtil.dumpBundle(bundleContext)}: Start tracking services with filter [${filter}]")

    // Create tracker matching this filter
    _tracker = new ServiceTracker[S, S](bundleContext, filter, null) {

      override def addingService(ref: ServiceReference[S]) = {
          val service = context.getService(ref)
        log.debug(s"Bundle ${DominoUtil.dumpBundle(bundleContext)}: Adding service [${service}] for filter [${filter}]")
        val watcherEvent = ServiceWatcherEvent.AddingService(service, ServiceWatcherContext(_tracker, ref))
        f(watcherEvent)
        service
      }

      override def modifiedService(ref: ServiceReference[S], service: S) {
        log.debug(s"Bundle ${DominoUtil.dumpBundle(bundleContext)}: Modified service [${service}] for filter [${filter}]")
        val watcherEvent = ServiceWatcherEvent.ModifiedService(service, ServiceWatcherContext(_tracker, ref))
        f(watcherEvent)
      }

      override def removedService(ref: ServiceReference[S], service: S) {
        log.debug(s"Bundle ${DominoUtil.dumpBundle(bundleContext)}: Removed service [${service}] for filter [${filter}]")
        val watcherEvent = ServiceWatcherEvent.RemovedService(service, ServiceWatcherContext(_tracker, ref))
        try f(watcherEvent) finally context.ungetService(ref)
      }
    }

    // Open tracker
    _tracker.open()
  }

  def stop() {
    log.debug(s"Bundle ${DominoUtil.dumpBundle(bundleContext)}: Stop tracking services with filter [${filter}]")
    // Close tracker
    _tracker.close()
    _tracker = null
  }

}