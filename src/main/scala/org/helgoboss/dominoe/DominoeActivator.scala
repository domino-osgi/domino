package org.helgoboss.dominoe

import bundle_watching.{BundleWatching}
import configuration_watching.ConfigurationWatching
import logging.Logging
import org.helgoboss.capsule._
import service_consuming.ServiceConsuming
import service_providing.ServiceProviding
import service_watching.ServiceWatching

/**
 * Extend from this class if you want to build a bundle activator in a very comfortable Scalaish way with everything included.
 * Extending from this class instead of mixing in the separate Osgi* traits you need has the big advantage that you don't need to
 * recompile your bundle if internal changes are made to the Osgi* traits. You just would have to replace the osgi-additions bundle.
 */
abstract class DominoeActivator extends OsgiContext
    with CapsuleConvenience
    with BundleWatching
    with ConfigurationWatching
    with ServiceConsuming
    with Logging
    with ServiceProviding
    with ServiceWatching {

  protected val capsuleContext = this
  protected val serviceProviding = this
  protected val serviceConsuming = this
}