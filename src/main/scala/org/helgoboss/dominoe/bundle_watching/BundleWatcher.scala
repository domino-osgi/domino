package org.helgoboss.dominoe.bundle_watching

import org.helgoboss.module_support.ModuleContext
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
trait BundleWatcher {
  protected def moduleContext: ModuleContext
  protected def bundleContext: BundleContext

  def watchBundles(f: BundleWatcherEvent => Unit): BundleTracker = {
    val bw = new BundleWatcherModule(f, bundleContext)
    moduleContext.addModule(bw)
    bw.tracker
  }
}

class SimpleBundleWatcher(
    protected val moduleContext: ModuleContext,
    protected val bundleContext: BundleContext) extends BundleWatcher {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}