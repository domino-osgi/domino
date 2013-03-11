package org.helgoboss.dominoe

import org.osgi.framework.{ BundleContext, BundleActivator }

/**
 * A bundle activator which contains empty `start` and `stop` methods. This is the base
 * for the trait [[org.helgoboss.dominoe.OsgiContext]].
 */
trait EmptyBundleActivator extends BundleActivator {
  def start(context: BundleContext) {
  }

  def stop(context: BundleContext) {
  }
}