package domino.bundle_watching

import domino.capsule.CapsuleContext
import org.osgi.framework.{Bundle, BundleContext}
import org.osgi.util.tracker.BundleTracker

/**
 * Provides a convenient method to add a bundle watcher capsule to the current capsule scope.
 *
 * @groupname WatchBundles Watch bundles
 * @groupdesc WatchBundles Methods for reacting to bundle events
 */
trait BundleWatching {
  /** Dependency */
  protected def capsuleContext: CapsuleContext

  /** Dependency */
  protected def bundleContext: BundleContext

  /**
   * Reacts to bundle events with the given event handler.
   *
   * @group WatchBundles
   * @param f bundle event handler
   * @return underlying bundle tracker
   */
  def watchBundles(f: BundleWatcherEvent => Unit): BundleTracker[Bundle] = {
    val bw = new BundleWatcherCapsule(f, bundleContext)
    capsuleContext.addCapsule(bw)
    bw.tracker
  }
}

