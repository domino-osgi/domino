package org.helgoboss.dominoe.service_consuming

import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.OsgiContext

/**
 * A class version of the [[org.helgoboss.dominoe.service_consuming.ServiceConsuming]] trait. Use this if you
 * don't want to use the trait.
 */
class SimpleServiceConsuming(protected val bundleContext: BundleContext) extends ServiceConsuming {
  def this(osgiContext: OsgiContext) = this(osgiContext.bundleContext)
}
