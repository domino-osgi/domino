package domino.service_providing

import domino.capsule.Capsule
import org.osgi.framework.{ BundleContext, ServiceRegistration }
import domino.DominoUtil
import scala.reflect.runtime.universe._

/**
 * A capsule which registers an object in the OSGi service registry while the current capsule scope is active.
 *
 * @param types Types under which to register the given service in the OSGi registry
 * @param properties Service properties
 * @param bundleContext Bundle context
 * @param service The object to be registered
 * @tparam S Service type
 */
class ServiceProviderCapsule[S](
    types: Traversable[(String, Type)],
    properties: Seq[(String, Any)],
    bundleContext: BundleContext,
    service: S) extends Capsule {

  protected var _reg: ServiceRegistration[S] = _

  /**
   * Returns the service registration as long as the current capsule scope is active.
   */
  def reg = _reg

  def start() {
    // Create array of class names under which the service shall be registered
    val typeArray = types.map(_._1).toArray

    // Add generic types expression to properties if necessary
    val extendedProperties = DominoUtil.createGenericsExpression(types.map(_._2)) match {
      case Some(exp) =>
        (DominoUtil.GenericsExpressionKey -> exp) +: properties

      case None =>
        properties
    }

    // Translate properties from Scala to Java
    val javaPropertiesHashtable = new java.util.Hashtable[String, AnyRef]
    extendedProperties foreach { tuple =>
      javaPropertiesHashtable.put(tuple._1, tuple._2.asInstanceOf[AnyRef])
    }

    // Register service
    val tmp = bundleContext.registerService(typeArray, service, javaPropertiesHashtable)
    _reg = tmp.asInstanceOf[ServiceRegistration[S]]
  }

  def stop() {
    // Unregister
    try {
      _reg.unregister()
    } catch {
      case x: IllegalStateException =>
      // Do nothing. Was already unregistered.
    }
    _reg = null
  }
}