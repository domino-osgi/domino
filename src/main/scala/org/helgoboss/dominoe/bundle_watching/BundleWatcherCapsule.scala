package org.helgoboss.dominoe.bundle_watching

import org.helgoboss.capsule.Capsule
import org.osgi.util.tracker.BundleTracker
import org.osgi.framework.{BundleContext, BundleEvent, Bundle}


class BundleWatcherCapsule(f: BundleWatcherEvent => Unit, bundleContext: BundleContext) extends Capsule {
  /**
   * The bundle tracker used to implement this feature. Is available as long as the current scope is active.
   */
  var tracker: BundleTracker = _

  def start() {
    import Bundle._
    tracker = new BundleTracker(bundleContext, ACTIVE + INSTALLED + RESOLVED + STARTING + STOPPING + UNINSTALLED, null) {
      override def addingBundle(bundle: Bundle, event: BundleEvent) = {
        val watcherEvent = BundleWatcherEvent.AddingBundle(bundle, BundleWatcherContext(tracker))
        f(watcherEvent)
        bundle
      }

      override def modifiedBundle(bundle: Bundle, event: BundleEvent, obj: AnyRef) {
        val watcherEvent = BundleWatcherEvent.ModifiedBundle(bundle, BundleWatcherContext(tracker))
        f(watcherEvent)
      }

      override def removedBundle(bundle: Bundle, event: BundleEvent, obj: AnyRef) {
        val watcherEvent = BundleWatcherEvent.RemovedBundle(bundle, BundleWatcherContext(tracker))
        f(watcherEvent)
      }
    }
    tracker.open()
  }

  def stop() {
    tracker.close()
    tracker = null
  }
}