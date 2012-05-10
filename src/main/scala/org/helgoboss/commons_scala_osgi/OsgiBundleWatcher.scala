package org.helgoboss.commons_scala_osgi

import org.osgi.framework.{BundleContext, BundleActivator, Bundle, BundleEvent}
import org.osgi.util.tracker.BundleTracker
import org.helgoboss.module_support.Module

trait OsgiBundleWatcher extends OsgiContext with OsgiLogging {
    protected abstract sealed class BundleWatcherEvent(bundle: Bundle, context: BundleWatcherContext)
    protected case class AddingBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)
    protected case class ModifiedBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)
    protected case class RemovedBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)
    
    protected case class BundleWatcherContext(tracker: BundleTracker)
    
    protected class BundleWatcher(f: BundleWatcherEvent => Unit) extends Module {
        var tracker: BundleTracker = _
    
        def start {
            import Bundle._
            tracker = new BundleTracker(bundleContext, ACTIVE + INSTALLED + RESOLVED + STARTING + STOPPING + UNINSTALLED, null) {
                override def addingBundle(bundle: Bundle, event: BundleEvent) = {
                    val watcherEvent = AddingBundle(bundle, BundleWatcherContext(tracker))
                    f(watcherEvent)
                    bundle
                }
                
                override def modifiedBundle(bundle: Bundle, event: BundleEvent, obj: AnyRef) {
                    val watcherEvent = ModifiedBundle(bundle,BundleWatcherContext( tracker))
                    f(watcherEvent)
                }
                
                override def removedBundle(bundle: Bundle, event: BundleEvent, obj: AnyRef) {
                    val watcherEvent = RemovedBundle(bundle, BundleWatcherContext(tracker))
                    f(watcherEvent)
                }
            }
            tracker.open
        }
        
        def stop {
            tracker.close
            tracker = null
        }
    }
    
    protected def watchBundles(f: BundleWatcherEvent => Unit): BundleTracker = {
        val bw = new BundleWatcher(f)
        addModule(bw)
        bw.tracker
    }
}