package domino

import bundle_watching.BundleWatcherContext
import bundle_watching.BundleWatcherEvent.AddingBundle
import bundle_watching.BundleWatcherEvent.RemovedBundle
import bundle_watching.BundleWatcherEvent.ModifiedBundle
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import org.osgi.util.tracker.BundleTracker
import org.osgi.framework.Bundle

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
@RunWith(classOf[JUnitRunner])
class BundleWatchingSpec extends DominoActivator with WordSpecLike with ShouldMatchers {

  "Bundle watching" should {

    "make it possible to react on bundles coming and going" in {
      whenBundleActive {
        val tracker: BundleTracker[Bundle] = watchBundles {
          case AddingBundle(b: Bundle, c: BundleWatcherContext) =>
          case RemovedBundle(b: Bundle, c: BundleWatcherContext) =>
          case ModifiedBundle(b: Bundle, c: BundleWatcherContext) =>
        }
      }
    }
  }

}