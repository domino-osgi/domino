package org.helgoboss.dominoe.configuration_watching

import org.helgoboss.capsule.CapsuleContext
import org.osgi.framework.{ServiceRegistration, BundleContext}
import org.helgoboss.scala_osgi_metatype.interfaces.{ObjectClassDefinition, MetaTypeProvider}
import org.helgoboss.scala_osgi_metatype.builders.SingleMetaTypeProvider
import org.helgoboss.dominoe.{DominoeActivator, OsgiContext}
import org.helgoboss.dominoe.service_consuming.ServiceConsuming

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 09.03.13
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
trait ConfigurationWatching {
  protected def capsuleContext: CapsuleContext
  protected def bundleContext: BundleContext
  protected def serviceConsuming: ServiceConsuming

  def whenConfigurationActive(servicePid: String, metaTypeProvider: Option[MetaTypeProvider] = None)(f: (Map[String, Any]) => Unit): ServiceRegistration = {
    val s = new ConfigurationWatcherCapsule(servicePid, f, metaTypeProvider, serviceConsuming, bundleContext, capsuleContext)
    capsuleContext.addCapsule(s)
    s.reg
  }

  def whenConfigurationActive(objectClassDefinition: ObjectClassDefinition)(f: (Map[String, Any]) => Unit): ServiceRegistration = {
    val metaTypeProvider = new SingleMetaTypeProvider(objectClassDefinition)
    whenConfigurationActive(objectClassDefinition.id, Some(metaTypeProvider))(f)
  }

  def whenFactoryConfigurationActive(servicePid: String, name: String, metaTypeProvider: Option[MetaTypeProvider] = None)(f: (Map[String, Any], String) => Unit): ServiceRegistration = {
    val s = new FactoryConfigurationWatcherCapsule(servicePid, name, f, metaTypeProvider, serviceConsuming, bundleContext, capsuleContext)
    capsuleContext.addCapsule(s)
    s.reg
  }

  def whenFactoryConfigurationActive(objectClassDefinition: ObjectClassDefinition)(f: (Map[String, Any], String) => Unit): ServiceRegistration = {
    val metaTypeProvider = new SingleMetaTypeProvider(objectClassDefinition)
    whenFactoryConfigurationActive(objectClassDefinition.id, objectClassDefinition.name, Some(metaTypeProvider))(f)
  }


}

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