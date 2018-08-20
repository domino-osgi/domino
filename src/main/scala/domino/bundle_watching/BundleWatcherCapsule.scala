package domino.bundle_watching

import domino.DominoUtil
import domino.capsule.Capsule
import domino.logging.internal.DominoLogger
import org.osgi.framework.{ Bundle, BundleContext, BundleEvent }
import org.osgi.util.tracker.BundleTracker

/**
 * A capsule for watching bundles coming and going as long as the current capsule scope is active. Tracks
 * all state transitions and bundles. The custom object facility is not used.
 *
 * @constructor Creates the capsule with the given handler and bundle context.
 * @param f Bundle event handler
 * @param bundleContext Bundle context
 */
class BundleWatcherCapsule(f: BundleWatcherEvent => Unit, bundleContext: BundleContext) extends Capsule {

  private[this] var _tracker: BundleTracker[Bundle] = _

  private[this] val log = DominoLogger[this.type]

  /**
   * Returns the underlying bundle tracker used to implement this feature as long as the current scope is active.
   */
  def tracker = _tracker

  def start() {
    log.debug(s"Bundle ${DominoUtil.dumpBundle(bundleContext)}: Start tracking bundle lifecycle events")

    import org.osgi.framework.Bundle._

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
    log.debug(s"Bundle ${DominoUtil.dumpBundle(bundleContext)}: Stop tracking bundle lifecycle events")
    // Stop tracking
    _tracker.close()
    _tracker = null
  }
}