package org.helgoboss.dominoe.configuration_watching

import org.helgoboss.scala_osgi_metatype.interfaces.MetaTypeProvider
import org.osgi.service.cm.{ConfigurationAdmin, ManagedService}
import org.helgoboss.capsule.{CapsuleContext, CapsuleScope, Capsule}
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider}
import org.helgoboss.scala_osgi_metatype.interfaces.MetaTypeProvider
import org.helgoboss.scala_osgi_metatype.adapters.MetaTypeProviderAdapter
import org.osgi.framework.{BundleContext, Constants, ServiceRegistration}
import java.util.Dictionary
import org.helgoboss.dominoe.DominoeUtil
import org.helgoboss.dominoe.service_consuming.ServiceConsuming

class ConfigurationWatcherCapsule(
    servicePid: String,
    f: Map[String, Any] => Unit, metaTypeProvider: Option[MetaTypeProvider],
    serviceConsuming: ServiceConsuming,
    bundleContext: BundleContext,
    capsuleContext: CapsuleContext
  ) extends ManagedService with Capsule with JMetaTypeProvider {

  lazy val metaTypeProviderAdapter = metaTypeProvider map { new MetaTypeProviderAdapter(_) }

  lazy val interfacesArray: Array[String] = Array(classOf[ManagedService].getName) ++ (
    metaTypeProvider map { p => classOf[JMetaTypeProvider].getName }
    )

  var reg: ServiceRegistration = _

  var capsuleScope: Option[CapsuleScope] = None

  var oldOptConf: Option[Dictionary[_, _]] = None

  def start() {
    val propertiesMap = Map(Constants.SERVICE_PID -> servicePid)

    // At first execute inner block synchronously with current configuration.
    val optConf = serviceConsuming.withService[ConfigurationAdmin, Option[Dictionary[_, _]]] {
      case Some(confAdmin) =>
        Option(confAdmin.getConfiguration(servicePid).getProperties)

      case None =>
        None
    }
    executeBlockWithConf(optConf)

    /* Then register managed service. This will cause ConfigurationAdmin push the current configuration in a separate thread
and call updated(). In updated(), we prevent the execution of the inner block, if the configuration stayed the same. */
    reg = bundleContext.registerService(interfacesArray, this, DominoeUtil.convertToDictionary(propertiesMap))
  }

  def stop() {
    capsuleScope foreach { _.stop() }
    reg.unregister()
    reg = null
  }

  def updated(conf: Dictionary[_, _]) {
    // See http://www.mail-archive.com/users@felix.apache.org/msg06764.html
    // We really need the right webservice URL here. This might be already important on first OSGi startup.
    // Therefore we query the config admin directly because the user can make sure then that the config value is already set.
    val safeOptConf = Option(conf) orElse getConfigDirectly()

    executeBlockWithConfIfChanged(safeOptConf)
  }

  private def executeBlockWithConfIfChanged(optConf: Option[Dictionary[_, _]]) {
    if (oldOptConf != optConf) {
      executeBlockWithConf(optConf)
    }
  }

  private def executeBlockWithConf(optConf: Option[Dictionary[_, _]]) {
    // Stop previous capsules
    capsuleScope foreach { _.stop() }

    // Start new capsules
    capsuleScope = Some(capsuleContext.executeWithinNewCapsuleScope {
      optConf match {
        case Some(conf) =>
          f(DominoeUtil.convertToMap(conf))

        case None =>
          f(Map.empty)
      }
    })

    // Save old conf
    oldOptConf = optConf
  }

  private def getConfigDirectly(): Option[Dictionary[_, _]] = {
    serviceConsuming.withService[ConfigurationAdmin, Option[Dictionary[_, _]]] {
      case Some(confAdmin) =>
        Option(confAdmin.getConfiguration(servicePid)) match {
          case Some(c) => Option(c.getProperties)
          case None => None
        }

      case None => None
    }
  }

  def getObjectClassDefinition(id: String, locale: String) = {
    metaTypeProviderAdapter map { _.getObjectClassDefinition(id, locale) } orNull
  }

  def getLocales = metaTypeProviderAdapter map { _.getLocales } orNull
}