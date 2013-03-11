package org.helgoboss.dominoe.bundle_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.{Bundle, BundleContext}
import org.osgi.util.tracker.BundleTracker

/**
 * Provides a convenient method to add a bundle watcher to the current scope.
 */
trait BundleWatching {
  /** Dependency */
  protected def capsuleContext: CapsuleContext

  /** Dependency */
  protected def bundleContext: BundleContext

  /**
   * Reacts to bundle events with the given event handler.
   *
   * @param f bundle event handler
   * @return underlying bundle tracker
   */
  def watchBundles(f: BundleWatcherEvent => Unit): BundleTracker[Bundle] = {
    val bw = new BundleWatcherCapsule(f, bundleContext)
    capsuleContext.addCapsule(bw)
    bw.tracker
  }
}

