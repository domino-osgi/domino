package org.helgoboss.domino.configuration_watching

import org.osgi.service.cm.{Configuration, ConfigurationAdmin, ManagedServiceFactory}
import org.helgoboss.capsule.{CapsuleContext, CapsuleScope, Capsule}
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider}
import org.helgoboss.scala_osgi_metatype.interfaces.MetaTypeProvider
import org.osgi.framework.{BundleContext, Constants, ServiceRegistration}
import org.helgoboss.scala_osgi_metatype.adapters.MetaTypeProviderAdapter
import java.util.Dictionary
import org.helgoboss.domino.DominoUtil
import org.helgoboss.domino.service_consuming.ServiceConsuming


/**
 * A capsule which allows easy access to the [[org.osgi.service.cm.ConfigurationAdmin]] functionality related
 * to factory configurations. 
 *
 * @param servicePid Service PID
 * @param name Descriptive name of the factory
 * @param f Handler
 * @param metaTypeProvider Optional metatype provider
 * @param serviceConsuming Dependency
 * @param bundleContext Dependency
 * @param capsuleContext Dependency
 */
class FactoryConfigurationWatcherCapsule(
    servicePid: String,
    name: String,
    f: (Map[String, Any], String) => Unit,
    metaTypeProvider: Option[MetaTypeProvider],
    serviceConsuming: ServiceConsuming,
    bundleContext: BundleContext,
    capsuleContext: CapsuleContext) extends AbstractConfigurationWatcherCapsule(metaTypeProvider) with ManagedServiceFactory {


  protected var _reg: ServiceRegistration[ManagedServiceFactory] = _

  /**
   * Returns the service registration of the factory configuration listener as long as the current scope is active.
   */
  def reg = _reg

  /**
   * Contains the interfaces under which this object will be put in the service registry.
   */
  lazy val interfacesArray: Array[String] = Array(classOf[ManagedServiceFactory].getName) ++ (
    metaTypeProvider map { p => classOf[JMetaTypeProvider].getName })


  /**
   * Contains the created capsule scopes qualified by the service factory PID.
   */
  protected var newCapsuleScopes = new collection.mutable.HashMap[String, CapsuleScope]

  /**
   * Contains the previous configuration maps. Used to determine whether the configuration has changed.
   */
  protected var oldOptConfs = new collection.mutable.HashMap[String, Option[Dictionary[String, _]]]

  def start() {
    // Service properties
    val propertiesMap = Map(Constants.SERVICE_PID -> servicePid)

    // Find out current configurations by pulling them
    val configurations = getConfigsDirectly()

    // At first execute inner block synchronously with each configuration
    configurations foreach { configuration =>
      executeBlockWithConf(configuration.getFactoryPid, Option(configuration.getProperties))
    }

    // Then register managed service factory
    val tmp = bundleContext.registerService(interfacesArray, this, DominoUtil.convertToDictionary(propertiesMap))
    _reg = tmp.asInstanceOf[ServiceRegistration[ManagedServiceFactory]]
  }

  def stop() {
    // Stop capsules in all newly created capsule scopes
    newCapsuleScopes.keys foreach { stopAndRemoveCapsuleScope }

    // Unregister managed service factory
    _reg.unregister()
    _reg = null
  }

  def getName = name

  def updated(pid: String, conf: Dictionary[String, _]) {
    // Execute handler only if configuration has changed
    executeBlockWithConfIfChanged(pid, Option(conf))
  }

  /**
   * Executes the handler only if the configuration has changed compared to the one which was used last.
   */
  protected def executeBlockWithConfIfChanged(pid: String, optConf: Option[Dictionary[String, _]]) {
    if (oldOptConfs.get(pid) != Some(optConf)) {
      executeBlockWithConf(pid, optConf)
    }
  }

  /**
   * Executes the correct handler with the given configuration and saves the configuration for future comparison.
   */
  private def executeBlockWithConf(pid: String, optConf: Option[Dictionary[String, _]]) {
    // If factory configuration was changed, we need to stop the capsules in the corresponding capsule scope first
    if (newCapsuleScopes.contains(pid)) {
      // Existing service is reconfigured. So we have to stop the capsule corresponding to the PID first.
      stopAndRemoveCapsuleScope(pid)
    }

    // Start capsules in new scope
    val newCapsuleScope = capsuleContext.executeWithinNewCapsuleScope {
      optConf match {
        case Some(conf) =>
          // Execute handler
          f(DominoUtil.convertToMap(conf), pid)

        case None =>
          // No configuration there. We use an empty map.
          f(Map.empty, pid)
      }
    }
    addCapsuleScope(pid, newCapsuleScope, optConf)
  }

  /**
   * Pulls the current configurations from the configuration admin.
   */
  protected def getConfigsDirectly() = {
    serviceConsuming.withService[ConfigurationAdmin, Traversable[Configuration]] {
      case Some(confAdmin) =>
        Option(confAdmin.listConfigurations("(service.pid=" + servicePid + ")")) match {
          case Some(cs) => cs
          case None => Nil
        }

      case None =>
        Nil
    }
  }

  def deleted(pid: String) {
    stopAndRemoveCapsuleScope(pid)
  }
  
  /**
   * Adds a capsule scope for the given PID.
   */
  protected def addCapsuleScope(pid: String, capsuleScope: CapsuleScope, optConf: Option[Dictionary[String, _]]) {
    newCapsuleScopes += (pid -> capsuleScope)
    oldOptConfs += (pid -> optConf)
  }

  /**
   * Stops the capsule scope for the given PID.
   */
  protected def stopAndRemoveCapsuleScope(pid: String) {
    val capsuleScope = newCapsuleScopes(pid)
    capsuleScope.stop()
    newCapsuleScopes -= pid
    oldOptConfs -= pid
  }
}