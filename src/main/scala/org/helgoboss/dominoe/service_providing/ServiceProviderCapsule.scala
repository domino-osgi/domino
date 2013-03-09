package org.helgoboss.dominoe.service_providing

import org.helgoboss.capsule.Capsule
import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.helgoboss.dominoe.DominoeUtil


class ServiceProviderCapsule(
    manifests: List[ClassManifest[_]],
    properties: Seq[(String, Any)],
    bundleContext: BundleContext,
    service: Any) extends Capsule {
  var reg: ServiceRegistration = _

  def start() {
    // Create OBJECTCLASS array
    val interfaceArray = manifests map { _.erasure.getName } toArray

    // Add complete types expression to properties if necessary
    val extendedProperties = DominoeUtil.createCompleteTypesExpression(manifests) match {
      case Some(e) =>
        (DominoeUtil.CompleteTypesExpressionKey -> e) +: properties

      case None =>
        properties
    }

    // Translate properties from Scala to Java
    val javaPropertiesHashtable = new java.util.Hashtable[String, AnyRef]
    extendedProperties foreach { tuple =>
      javaPropertiesHashtable.put(tuple._1, tuple._2.asInstanceOf[AnyRef])
    }

    // Register service
    reg = bundleContext.registerService(interfaceArray, service, javaPropertiesHashtable)
  }

  def stop() {
    try {
      reg.unregister()
    } catch {
      case x: IllegalStateException =>
      // Do nothing. Was already unregistered.
    }
    reg = null
  }
}