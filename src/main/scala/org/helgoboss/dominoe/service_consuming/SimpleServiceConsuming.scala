package org.helgoboss.dominoe.service_consuming

import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.OsgiContext

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 10.03.13
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
class SimpleServiceConsuming(protected val bundleContext: BundleContext) extends ServiceConsuming {
  def this(osgiContext: OsgiContext) = this(osgiContext.bundleContext)
}
