package org.helgoboss.dominoe.service_providing

import org.helgoboss.capsule.Capsule
import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.helgoboss.dominoe.DominoeUtil


/**
 * A capsule which registers an object in the OSGi service registry while the current scope is active.
 *
 * @param manifests manifests for types under which to register the given service in the OSGi registry
 * @param properties service properties
 * @param bundleContext bundle context
 * @param service the object to be registered
 * @tparam S service type
 */
class ServiceProviderCapsule[S](
    manifests: List[ClassManifest[_]],
    properties: Seq[(String, Any)],
    bundleContext: BundleContext,
    service: S) extends Capsule {

  protected var _reg: ServiceRegistration[S] = _

  /**
   * Returns the service registration.
   */
  def reg = _reg

  def start() {
    // Create array of class names under which the service shall be registered
    val interfaceArray = manifests map { _.erasure.getName } toArray

    // Add generic types expression to properties if necessary
    val extendedProperties = DominoeUtil.createGenericsExpression(manifests) match {
      case Some(exp) =>
        (DominoeUtil.CompleteTypesExpressionKey -> exp) +: properties

      case None =>
        properties
    }

    // Translate properties from Scala to Java
    val javaPropertiesHashtable = new java.util.Hashtable[String, AnyRef]
    extendedProperties foreach { tuple =>
      javaPropertiesHashtable.put(tuple._1, tuple._2.asInstanceOf[AnyRef])
    }

    // Register service
    val tmp = bundleContext.registerService(interfaceArray, service, javaPropertiesHashtable)
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