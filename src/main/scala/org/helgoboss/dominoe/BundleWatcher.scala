package org.helgoboss.dominoe

import org.osgi.framework.{ BundleContext, Bundle, BundleEvent }
import org.osgi.util.tracker.BundleTracker
import org.helgoboss.module_support._

class SimpleBundleWatcher(
    protected val moduleContext: ModuleContext,
    protected val bundleContext: BundleContext) extends BundleWatcher {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}

trait BundleWatcher {
  protected def moduleContext: ModuleContext
  protected def bundleContext: BundleContext

  private class BundleWatcher(f: BundleWatcherEvent => Unit) extends Module {
    var tracker: BundleTracker = _

    def start() {
      import Bundle._
      tracker = new BundleTracker(bundleContext, ACTIVE + INSTALLED + RESOLVED + STARTING + STOPPING + UNINSTALLED, null) {
        override def addingBundle(bundle: Bundle, event: BundleEvent) = {
          val watcherEvent = AddingBundle(bundle, BundleWatcherContext(tracker))
          f(watcherEvent)
          bundle
        }

        override def modifiedBundle(bundle: Bundle, event: BundleEvent, obj: AnyRef) {
          val watcherEvent = ModifiedBundle(bundle, BundleWatcherContext(tracker))
          f(watcherEvent)
        }

        override def removedBundle(bundle: Bundle, event: BundleEvent, obj: AnyRef) {
          val watcherEvent = RemovedBundle(bundle, BundleWatcherContext(tracker))
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

  def watchBundles(f: BundleWatcherEvent => Unit): BundleTracker = {
    val bw = new BundleWatcher(f)
    moduleContext.addModule(bw)
    bw.tracker
  }
}