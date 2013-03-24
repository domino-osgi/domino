package org.helgoboss.domino.configuration_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.domino.service_consuming.ServiceConsuming
import org.helgoboss.domino.{DominoActivator, OsgiContext}

/**
 * A class that mixes in the [[ConfigurationWatching]] trait. Use this if you want to use a class instead of a trait.
 */
class SimpleConfigurationWatching(
    protected val capsuleContext: CapsuleContext,
    protected val bundleContext: BundleContext,
    protected val serviceConsuming: ServiceConsuming) extends ConfigurationWatching {

  def this(osgiContext: OsgiContext, serviceConsuming: ServiceConsuming) = this(osgiContext, osgiContext.bundleContext, serviceConsuming)

  def this(DominoActivator: DominoActivator) = this(
    DominoActivator,
    DominoActivator.bundleContext,
    DominoActivator)
}
