package org.helgoboss.dominoe.configuration_watching

import org.helgoboss.module_support.ModuleContext
import org.osgi.framework.{ServiceRegistration, BundleContext}
import org.helgoboss.dominoe.service_consuming.ServiceConsumer
import org.helgoboss.scala_osgi_metatype.interfaces.{ObjectClassDefinition, MetaTypeProvider}
import org.helgoboss.scala_osgi_metatype.builders.SingleMetaTypeProvider
import org.helgoboss.dominoe.{DominoeActivator, OsgiContext}

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 09.03.13
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
trait ConfigurationWatcher {
  protected def moduleContext: ModuleContext
  protected def bundleContext: BundleContext
  protected def serviceConsumer: ServiceConsumer

  def whenConfigurationActive(servicePid: String, metaTypeProvider: Option[MetaTypeProvider] = None)(f: (Option[Map[String, Any]]) => Unit): ServiceRegistration = {
    val s = new ConfigurationWatcherModule(servicePid, f, metaTypeProvider, serviceConsumer, bundleContext, moduleContext)
    moduleContext.addModule(s)
    s.reg
  }

  def whenConfigurationActive(objectClassDefinition: ObjectClassDefinition)(f: (Option[Map[String, Any]]) => Unit): ServiceRegistration = {
    val metaTypeProvider = new SingleMetaTypeProvider(objectClassDefinition)
    whenConfigurationActive(objectClassDefinition.id, Some(metaTypeProvider))(f)
  }

  def whenFactoryConfigurationActive(servicePid: String, name: String, metaTypeProvider: Option[MetaTypeProvider] = None)(f: (Option[Map[String, Any]], String) => Unit): ServiceRegistration = {
    val s = new FactoryConfigurationWatcherModule(servicePid, name, f, metaTypeProvider, serviceConsumer, bundleContext, moduleContext)
    moduleContext.addModule(s)
    s.reg
  }

  def whenFactoryConfigurationActive(objectClassDefinition: ObjectClassDefinition)(f: (Option[Map[String, Any]], String) => Unit): ServiceRegistration = {
    val metaTypeProvider = new SingleMetaTypeProvider(objectClassDefinition)
    whenFactoryConfigurationActive(objectClassDefinition.id, objectClassDefinition.name, Some(metaTypeProvider))(f)
  }


}

class SimpleConfigurationWatcher(
    protected val moduleContext: ModuleContext,
    protected val bundleContext: BundleContext,
    protected val serviceConsumer: ServiceConsumer) extends ConfigurationWatcher {

  def this(osgiContext: OsgiContext, serviceConsumer: ServiceConsumer) = this(osgiContext, osgiContext.bundleContext, serviceConsumer)

  def this(DominoeActivator: DominoeActivator) = this(
    DominoeActivator,
    DominoeActivator.bundleContext,
    DominoeActivator)
}