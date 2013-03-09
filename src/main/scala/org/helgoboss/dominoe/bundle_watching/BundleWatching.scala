package org.helgoboss.dominoe.bundle_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.BundleTracker
import org.helgoboss.dominoe.OsgiContext

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 09.03.13
 * Time: 22:19
 * To change this template use File | Settings | File Templates.
 */
trait BundleWatching {
  protected def capsuleContext: CapsuleContext
  protected def bundleContext: BundleContext

  def watchBundles(f: BundleWatcherEvent => Unit): BundleTracker = {
    val bw = new BundleWatcherCapsule(f, bundleContext)
    capsuleContext.addCapsule(bw)
    bw.tracker
  }
}

