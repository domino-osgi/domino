package org.helgoboss.dominoe

import org.helgoboss.module_support._

/**
 * Extend from this class if you want to build a bundle activator in a very comfortable Scalaish way with everything included.
 * Extending from this class instead of mixing in the separate Osgi* traits you need has the big advantage that you don't need to
 * recompile your bundle if internal changes are made to the Osgi* traits. You just would have to replace the osgi-additions bundle.
 */
abstract class DominoeActivator extends OsgiContext
    with ModuleConvenience
    with BundleWatcher
    with ConfigurationWatcher
    with ServiceConsumer
    with Logging
    with MetaTypeProvider
    with ServiceProvider
    with ServiceWatcher {

  protected val moduleContext = this
  protected val serviceProvider = this
  protected val serviceConsumer = this
}