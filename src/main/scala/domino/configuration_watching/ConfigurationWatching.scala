package domino.configuration_watching

import domino.capsule.CapsuleContext
import org.osgi.framework.{ServiceRegistration, BundleContext}
import domino.scala_osgi_metatype.interfaces.{ObjectClassDefinition, MetaTypeProvider}
import domino.scala_osgi_metatype.builders.SingleMetaTypeProvider
import domino.service_consuming.ServiceConsuming
import org.osgi.service.cm.{ManagedServiceFactory, ManagedService}

/**
 * Provides convenient methods to add a configuration or factory configuration watcher capsule to the current capsule scope.
 *
 * @groupname WatchConfigurations Watch configurations
 * @groupdesc WatchConfigurations Methods for listening to configuration updates
 * @groupname WatchFactoryConfigurations Watch factory configurations
 * @groupdesc WatchFactoryConfigurations Methods for listening to factory configuration additions, updates and removals
 */
trait ConfigurationWatching {
  /** Dependency */
  protected def capsuleContext: CapsuleContext

  /** Dependency */
  protected def bundleContext: BundleContext

  /** Dependency */
  protected def serviceConsuming: ServiceConsuming

  /**
   * Executes the given handler with the initial configuration or an empty map if none exists. Whenever
   * the configuration is changed, the capsules registered in the handler are stopped and the handler is executed
   * again with the new configuration.
   *
   * @group WatchConfigurations
   * @param servicePid service PID
   * @param metaTypeProvider optional metatype provider
   * @param f handler
   * @return the managed service registration
   */
  def whenConfigurationActive(servicePid: String, metaTypeProvider: Option[MetaTypeProvider] = None)
                             (f: (Map[String, Any]) => Unit): ServiceRegistration[ManagedService] = {
    val s = new ConfigurationWatcherCapsule(servicePid, f, metaTypeProvider, serviceConsuming, bundleContext,
      capsuleContext)
    capsuleContext.addCapsule(s)
    s.reg
  }

  /**
   * Like the same-named method which expects the service PID but takes the service PID from the given object class
   * definition and registers a corresponding meta type provider so a nice configuration GUI will be created.
   *
   * @group WatchConfigurations
   * @param objectClassDefinition object class definition
   * @param f handler
   * @return the managed service registration
   */
  def whenConfigurationActive(objectClassDefinition: ObjectClassDefinition)
                             (f: (Map[String, Any]) => Unit): ServiceRegistration[ManagedService] = {
    val metaTypeProvider = new SingleMetaTypeProvider(objectClassDefinition)
    whenConfigurationActive(objectClassDefinition.id, Some(metaTypeProvider))(f)
  }

  /**
   * Executes the given handler whenever a new factory configuration is created. Whenever a factory configuration
   * is changed, the correct capsules registered in the corresponding handler are stopped and the handler is
   * executed again with the new factory configuration. When the factory configuration is removed, the corresponding
   * capsules are stopped.
   *
   * @group WatchFactoryConfigurations
   * @param servicePid service PID
   * @param name descriptive name for the factory
   * @param metaTypeProvider optional metatype provider
   * @param f handler
   * @return the managed service factory registration
   */
  def whenFactoryConfigurationActive(servicePid: String, name: String, metaTypeProvider: Option[MetaTypeProvider] = None)
                                    (f: (Map[String, Any], String) => Unit): ServiceRegistration[ManagedServiceFactory] = {
    val s = new FactoryConfigurationWatcherCapsule(servicePid, name, f, metaTypeProvider, serviceConsuming,
      bundleContext, capsuleContext)
    capsuleContext.addCapsule(s)
    s.reg
  }

  /**
   * Like the same-named method which expects the service PID but takes the service PID from the given object class
   * definition and registers a corresponding meta type provider so a nice configuration GUI will be created.
   *
   * @group WatchFactoryConfigurations
   * @param objectClassDefinition object class definition
   * @param f handler
   * @return the managed service factory registration
   */
  def whenFactoryConfigurationActive(objectClassDefinition: ObjectClassDefinition)
                                    (f: (Map[String, Any], String) => Unit): ServiceRegistration[ManagedServiceFactory] = {
    val metaTypeProvider = new SingleMetaTypeProvider(objectClassDefinition)
    whenFactoryConfigurationActive(objectClassDefinition.id, objectClassDefinition.name, Some(metaTypeProvider))(f)
  }
}

