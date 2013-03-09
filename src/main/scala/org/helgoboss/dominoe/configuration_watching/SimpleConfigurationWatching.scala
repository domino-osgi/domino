package org.helgoboss.dominoe.configuration_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import org.helgoboss.dominoe.service_consuming.ServiceConsuming
import org.helgoboss.dominoe.{DominoeActivator, OsgiContext}

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 10.03.13
 * Time: 00:15
 * To change this template use File | Settings | File Templates.
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
