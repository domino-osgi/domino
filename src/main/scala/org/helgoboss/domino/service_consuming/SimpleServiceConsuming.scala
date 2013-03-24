package org.helgoboss.domino.service_consuming

import org.osgi.framework.BundleContext
import org.helgoboss.domino.OsgiContext

/**
 * A class that mixes in the [[ServiceConsuming]] trait. Use this if you want to use a class instead of a trait.
 */
class SimpleServiceConsuming(protected val bundleContext: BundleContext) extends ServiceConsuming {
  def this(osgiContext: OsgiContext) = this(osgiContext.bundleContext)
}
