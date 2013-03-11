package org.helgoboss.dominoe.bundle_watching

import org.osgi.framework.Bundle

/**
 * Super class for bundle watcher events.
 *
 * @param bundle the bundle affected by the state transition
 * @param context additional event data
 */
abstract sealed class BundleWatcherEvent(bundle: Bundle, context: BundleWatcherContext)

/**
 * Contains the bundle watcher events.
 */
object BundleWatcherEvent {

  /**
   *  A bundle is being added to the BundleTracker.
   */
  case class AddingBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)

  /**
   * A bundle tracked by the BundleTracker has been modified.
   */
  case class ModifiedBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)

  /**
   *  A bundle tracked by the BundleTracker has been removed.
   */
  case class RemovedBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)
}
