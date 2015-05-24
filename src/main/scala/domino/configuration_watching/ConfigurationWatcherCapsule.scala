package domino.configuration_watching

import org.osgi.service.cm.{ConfigurationAdmin, ManagedService}
import domino.capsule.{CapsuleContext, CapsuleScope}
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider}
import domino.scala_osgi_metatype.interfaces.MetaTypeProvider
import org.osgi.framework.{BundleContext, Constants, ServiceRegistration}
import java.util.Dictionary
import domino.DominoUtil
import domino.service_consuming.ServiceConsuming

/**
 * A capsule which allows easy access to the [[org.osgi.service.cm.ConfigurationAdmin]] functionality related
 * to normal configurations (not factory configurations).
 *
 * @param servicePid Service PID
 * @param f Handler which is executed initially and on every configuration change
 * @param metaTypeProvider Optional metatype provider
 * @param serviceConsuming Dependency
 * @param bundleContext Dependency
 * @param capsuleContext Dependency
 */
class ConfigurationWatcherCapsule(
    servicePid: String,
    f: Map[String, Any] => Unit,
    metaTypeProvider: Option[MetaTypeProvider],
    serviceConsuming: ServiceConsuming,
    bundleContext: BundleContext,
    capsuleContext: CapsuleContext
  ) extends AbstractConfigurationWatcherCapsule(metaTypeProvider) with ManagedService {

  protected var _reg: ServiceRegistration[ManagedService] = _

  /**
   * Returns the service registration of the configuration listener as long as the current scope is active.
   */
  def reg = _reg

  /**
   * Contains the interfaces under which this object will be put in the service registry.
   */
  protected lazy val interfacesArray: Array[String] = Array(classOf[ManagedService].getName) ++ (
    metaTypeProvider map { p => classOf[JMetaTypeProvider].getName })

  /**
   * Contains the new capsule scope.
   */
  protected var newCapsuleScope: Option[CapsuleScope] = None

  /**
   * Contains the previous configuration map. Used to determine whether the configuration has changed.
   */
  protected var oldOptConf: Option[Dictionary[String, _]] = None

  def start() {
    // Service properties
    val propertiesMap = Map(Constants.SERVICE_PID -> servicePid)

    // Find out current configuration by pulling it
    val optConf = getConfigDirectly

    // At first execute inner block synchronously with current configuration. Even if configuration admin is not present.
    executeBlockWithConf(optConf)

    // Register managed service. This will cause ConfigurationAdmin push the current configuration in a separate
    // thread and call updated(). In updated(), we prevent the second execution of the inner block because we check
    // whether the configuration is still the same.
    val tmp = bundleContext.registerService(interfacesArray, this, DominoUtil.convertToDictionary(propertiesMap))
    _reg = tmp.asInstanceOf[ServiceRegistration[ManagedService]]
  }

  def stop() {
    // Stop capsules in the newly created capsule scope
    newCapsuleScope foreach { _.stop() }

    // Unregister managed service
    _reg.unregister()
    _reg = null
  }

  def updated(conf: Dictionary[String, _]) {
    // We query the config admin directly because the user can make sure then that the config value is already set.
    // See http://www.mail-archive.com/users@felix.apache.org/msg06764.html
    val safeOptConf = Option(conf) orElse getConfigDirectly

    // Execute handler only if configuration has changed
    executeBlockWithConfIfChanged(safeOptConf)
  }

  /**
   * Executes the handler only if the configuration has changed compared to the one which was used last.
   */
  protected def executeBlockWithConfIfChanged(optConf: Option[Dictionary[String, _]]) {
    if (oldOptConf != optConf) {
      executeBlockWithConf(optConf)
    }
  }
  
  /**
   * Executes the handler with the given configuration and saves it for future comparison.
   */
  protected def executeBlockWithConf(optConf: Option[Dictionary[String, _]]) {
    // Stop capsules in previous scope
    newCapsuleScope foreach { _.stop() }

    // Start capsules in new scope
    newCapsuleScope = Some(capsuleContext.executeWithinNewCapsuleScope {
      optConf match {
        case Some(conf) =>
          // Execute handler
          f(DominoUtil.convertToMap(conf))

        case None =>
          // No configuration there. We use an empty map.
          f(Map.empty)
      }
    })

    // Save old conf
    oldOptConf = optConf
  }

  /**
   * Pulls the current configuration from the configuration admin.
   */
  protected def getConfigDirectly: Option[Dictionary[String, _]] = {
    serviceConsuming.withService[ConfigurationAdmin, Option[Dictionary[String, _]]] {
      case Some(confAdmin) =>
        Option(confAdmin.getConfiguration(servicePid)) match {
          case Some(c) => Option(c.getProperties)
          case None => None
        }

      case None => None
    }
  }
}