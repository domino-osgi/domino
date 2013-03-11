package org.helgoboss.dominoe.bundle_watching

import org.helgoboss.capsule.Capsule
import org.osgi.util.tracker.BundleTracker
import org.osgi.framework.{BundleContext, BundleEvent, Bundle}

/**
 * A capsule for watching bundles coming and going as long as the current scope is active. Tracks all
 * all state transitions and bundles. The custom object facility is not used.
 *
 * @param f bundle event handler
 * @param bundleContext bundle context
 */
class BundleWatcherCapsule(f: BundleWatcherEvent => Unit, bundleContext: BundleContext) extends Capsule {
  protected var _tracker: BundleTracker[Bundle] = _

  /**
   * Returns the bundle tracker used to implement this feature. It is available as long as the current scope is active
   * and becomes `null` after that.
   */
  def tracker = _tracker

  def start() {
    import Bundle._

    // Create a bundle tracker which tracks all bundle state transitions
    _tracker = new BundleTracker[Bundle](bundleContext, ACTIVE + INSTALLED + RESOLVED + STARTING + STOPPING + UNINSTALLED, null) {
      override def addingBundle(bundle: Bundle, event: BundleEvent) = {
        val watcherEvent = BundleWatcherEvent.AddingBundle(bundle, BundleWatcherContext(_tracker))
        f(watcherEvent)
        bundle
      }

      override def modifiedBundle(bundle: Bundle, event: BundleEvent, obj: Bundle) {
        val watcherEvent = BundleWatcherEvent.ModifiedBundle(bundle, BundleWatcherContext(_tracker))
        f(watcherEvent)
      }

      override def removedBundle(bundle: Bundle, event: BundleEvent, obj: Bundle) {
        val watcherEvent = BundleWatcherEvent.RemovedBundle(bundle, BundleWatcherContext(_tracker))
        f(watcherEvent)
      }
    }

    // Start tracking
    _tracker.open()
  }

  def stop() {
    // Stop tracking
    _tracker.close()
    _tracker = null
  }
}