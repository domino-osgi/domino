package domino.bundle_watching

import org.osgi.framework.Bundle

/**
 * Super class for bundle watcher events. The possible events are defined in the companion object.
 *
 * @param bundle Bundle affected by the state transition
 * @param context Additional event data
 */
abstract sealed class BundleWatcherEvent(bundle: Bundle, context: BundleWatcherContext)

/**
 * Contains the possible bundle watcher events.
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
