package org.helgoboss.dominoe.configuration_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.service_consuming.ServiceConsuming
import org.helgoboss.dominoe.{DominoeActivator, OsgiContext}

/**
 * A class version of the [[org.helgoboss.dominoe.configuration_watching.ConfigurationWatching]] trait. Use this
 * if you don't want to use the trait.
 */
class SimpleConfigurationWatching(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext,
    protected val serviceConsuming: ServiceConsuming) extends ConfigurationWatching {

  def this(osgiContext: OsgiContext, serviceConsuming: ServiceConsuming) = this(osgiContext, osgiContext.bundleContext, serviceConsuming)

  def this(DominoeActivator: DominoeActivator) = this(
    DominoeActivator,
    DominoeActivator.bundleContext,
    DominoeActivator)
}
