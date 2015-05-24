package domino.service_consuming

import org.osgi.framework.BundleContext
import domino.OsgiContext

/**
 * A class that mixes in the [[ServiceConsuming]] trait. Use this if you want to use a class instead of a trait.
 */
class SimpleServiceConsuming(protected val bundleContext: BundleContext) extends ServiceConsuming {
  def this(osgiContext: OsgiContext) = this(osgiContext.bundleContext)
}
