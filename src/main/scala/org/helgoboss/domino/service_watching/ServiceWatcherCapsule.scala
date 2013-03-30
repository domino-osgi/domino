package org.helgoboss.domino.service_watching

import org.helgoboss.capsule.Capsule
import org.osgi.util.tracker.ServiceTracker
import org.osgi.framework.{Filter, BundleContext, ServiceReference}

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
    bundleContext: BundleContext) extends Capsule {

  protected var _tracker: ServiceTracker[S, S] = _

  /**
   * Returns the underlying service tracker as long as the current capsule scope is active.
   */
  def tracker = _tracker

  def start() {
    // Create tracker matching this filter
    _tracker = new ServiceTracker[S, S](bundleContext, filter, null) {
      override def addingService(ref: ServiceReference[S]) = {
        val service = context.getService(ref)
        val watcherEvent = ServiceWatcherEvent.AddingService(service, ServiceWatcherContext(_tracker, ref))
        f(watcherEvent)
        service
      }

      override def modifiedService(ref: ServiceReference[S], service: S) {
        val watcherEvent = ServiceWatcherEvent.ModifiedService(service, ServiceWatcherContext(_tracker, ref))
        f(watcherEvent)
      }

      override def removedService(ref: ServiceReference[S], service: S) {
        val watcherEvent = ServiceWatcherEvent.RemovedService(service, ServiceWatcherContext(_tracker, ref))
        try f(watcherEvent) finally context ungetService ref
      }
    }

    // Open tracker
    _tracker.open()
  }

  def stop() {
    // Close tracker
    _tracker.close()
    _tracker = null
  }
}