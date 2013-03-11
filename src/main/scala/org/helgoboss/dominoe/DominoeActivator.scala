package org.helgoboss.dominoe

import bundle_watching.{BundleWatching}
import configuration_watching.ConfigurationWatching
import logging.Logging
import org.helgoboss.capsule._
import service_consuming.ServiceConsuming
import service_providing.ServiceProviding
import service_watching.ServiceWatching

/**
 * Let your bundle activator extend from this class to get full access to the Dominoe DSL.
 *
 * I encourage extending from this class instead of mixing in the single traits because then you don't need to
 * recompile your bundle if minor internal changes are made to Dominoe. This results in greater upwards compatibility.
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