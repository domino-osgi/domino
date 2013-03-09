package org.helgoboss.dominoe.configuration_watching

import org.osgi.service.cm.{Configuration, ConfigurationAdmin, ManagedServiceFactory}
import org.helgoboss.capsule.{CapsuleContext, CapsuleContainer, Capsule}
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider}
import org.helgoboss.scala_osgi_metatype.interfaces.MetaTypeProvider
import org.osgi.framework.{BundleContext, Constants, ServiceRegistration}
import org.helgoboss.scala_osgi_metatype.adapters.MetaTypeProviderAdapter
import java.util.Dictionary
import org.helgoboss.dominoe.DominoeUtil
import org.helgoboss.dominoe.service_consuming.ServiceConsuming


class FactoryConfigurationWatcherCapsule(
    servicePid: String,
    name: String,
    f: (Option[Map[String, Any]], String) => Unit,
    metaTypeProvider: Option[MetaTypeProvider],
    serviceConsuming: ServiceConsuming,
    bundleContext: BundleContext,
    capsuleContext: CapsuleContext) extends ManagedServiceFactory with Capsule with JMetaTypeProvider {

  var reg: ServiceRegistration = _

  lazy val metaTypeProviderAdapter = metaTypeProvider map { new MetaTypeProviderAdapter(_) }

  lazy val interfacesArray: Array[String] = Array(classOf[ManagedServiceFactory].getName) ++ (
    metaTypeProvider map { p => classOf[JMetaTypeProvider].getName }
    )

  var capsuleContainers = new collection.mutable.HashMap[String, CapsuleContainer]
  var oldOptConfs = new collection.mutable.HashMap[String, Option[Dictionary[_, _]]]

  def start() {
    val propertiesMap = Map(Constants.SERVICE_PID -> servicePid)

    // At first execute inner block synchronously with each configuration

    val configurations = serviceConsuming.withService[ConfigurationAdmin, Traversable[Configuration]] {
      case Some(confAdmin) =>
        Option(confAdmin.listConfigurations("(service.pid=" + servicePid + ")")) match {
          case Some(cs) => cs
          case None => Nil
        }

      case None =>
        Nil
    }

    configurations foreach { configuration =>
      executeBlockWithConf(configuration.getFactoryPid, Option(configuration.getProperties))
    }

    // Then register managed service factory
    reg = bundleContext.registerService(interfacesArray, this, DominoeUtil.convertToDictionary(propertiesMap))
  }

  def stop() {
    capsuleContainers.keys foreach { stopAndRemoveCapsuleContainer }
    reg.unregister()
    reg = null
  }

  def getName = name

  def updated(pid: String, conf: Dictionary[_, _]) {
    executeBlockWithConfIfChanged(pid, Option(conf))
  }


  private def executeBlockWithConfIfChanged(pid: String, optConf: Option[Dictionary[_, _]]) {
    if (oldOptConfs.get(pid) != Some(optConf)) {
      executeBlockWithConf(pid, optConf)
    }
  }

  private def executeBlockWithConf(pid: String, optConf: Option[Dictionary[_, _]]) {
    if (capsuleContainers.contains(pid)) {
      // Existing service is reconfigured. So we have to stop the capsule corresponding to the PID first.
      stopAndRemoveCapsuleContainer(pid)
    }
    val newCapsuleContainer = capsuleContext.executeWithinNewCapsuleContainer {
      optConf match {
        case Some(conf) =>
          f(Some(DominoeUtil.convertToMap(conf)), pid)

        case None =>
          f(None, pid)
      }
    }
    addCapsuleContainer(pid, newCapsuleContainer, optConf)
  }

  def deleted(pid: String) {
    stopAndRemoveCapsuleContainer(pid)
  }

  private def addCapsuleContainer(pid: String, capsuleContainer: CapsuleContainer, optConf: Option[Dictionary[_, _]]) {
    capsuleContainers += (pid -> capsuleContainer)
    oldOptConfs += (pid -> optConf)
  }

  private def stopAndRemoveCapsuleContainer(pid: String) {
    val capsuleContainer = capsuleContainers(pid)
    capsuleContainer.stop()
    capsuleContainers -= pid
    oldOptConfs -= pid
  }

  def getObjectClassDefinition(id: String, locale: String) = {
    metaTypeProviderAdapter map { _.getObjectClassDefinition(id, locale) } orNull
  }

  def getLocales = metaTypeProviderAdapter map { _.getLocales } orNull
}