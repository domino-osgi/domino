package domino

import org.osgi.framework.{ BundleContext, BundleActivator }

/**
 * A bundle activator which contains empty `start` and `stop` methods. This is the basis
 * for the trait [[OsgiContext]] which hooks into the `start` and `stop` methods.
 * 
 * @see [[http://www.artima.com/scalazine/articles/stackable_trait_pattern.html Stackable Trait Pattern]]
 */
trait EmptyBundleActivator extends BundleActivator {
  def start(context: BundleContext): Unit = {
  }

  def stop(context: BundleContext): Unit = {
  }
}