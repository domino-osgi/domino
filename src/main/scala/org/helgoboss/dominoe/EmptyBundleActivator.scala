package org.helgoboss.dominoe

import org.osgi.framework.{ BundleContext, BundleActivator }

trait EmptyBundleActivator extends BundleActivator {

  def start(context: BundleContext) {
  }

  def stop(context: BundleContext) {
  }
}