package org.helgoboss.dominoe.service_providing

import org.osgi.framework.{ BundleContext, ServiceRegistration }
import org.helgoboss.capsule._

/**
 * Offers methods to register the given service as OSGi service.
 *
 * @param service service
 * @param capsuleContext dependency
 * @param bundleContext dependency
 * @tparam S service type
 */
class ProvidableService[S](service: S, capsuleContext: CapsuleContext, bundleContext: BundleContext) {
  /**
   * Registers the service under the specified type with the given service properties.
   *
   * @param properties service properties
   * @tparam S1 type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest](properties: (String, Any)*): ServiceRegistration[S] = {
    providesServiceInternal(
      List(classManifest[S1]),
      properties
    )
  }

  /**
   * Registers the service under the specified type and without service properties.
   *
   * @tparam S1 type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest]: ServiceRegistration[S] = {
    providesService[S1]()
  }

  /**
   * Registers the service under the specified type with the given service properties.
   *
   * @param properties service properties
   * @tparam S1 type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest](properties: Map[String, Any]): ServiceRegistration[S] = {
    providesService[S1](properties.toSeq: _*)
  }

  /**
   * Registers the service under the specified types with the given service properties.
   *
   * @param properties service properties
   * @tparam S1 first type in OSGi service registry
   * @tparam S2 second type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest, S2 >: S: ClassManifest](properties: (String, Any)*): ServiceRegistration[S] = {
    providesServiceInternal(
      List(classManifest[S1], classManifest[S2]),
      properties
    )
  }

  /**
   * Registers the service under the specified types without service properties.
   *
   * @tparam S1 first type in OSGi service registry
   * @tparam S2 second type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest, S2 >: S: ClassManifest]: ServiceRegistration[S] = {
    providesService[S1, S2]()
  }

  /**
   * Registers the service under the specified types with the given service properties.
   *
   * @param properties service properties
   * @tparam S1 first type in OSGi service registry
   * @tparam S2 second type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest, S2 >: S: ClassManifest]
      (properties: Map[String, Any]): ServiceRegistration[S] = {

    providesService[S1, S2](properties.toSeq: _*)
  }

  /**
   * Registers the service under the specified types with the given service properties.
   *
   * @param properties service properties
   * @tparam S1 first type in OSGi service registry
   * @tparam S2 second type in OSGi service registry
   * @tparam S3 third type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest, S2 >: S: ClassManifest, S3 >: S: ClassManifest]
      (properties: (String, Any)*): ServiceRegistration[S] = {

    providesServiceInternal(
      List(classManifest[S1], classManifest[S2], classManifest[S3]),
      properties
    )
  }

  /**
   * Registers the service under the specified types without service properties.
   *
   * @tparam S1 first type in OSGi service registry
   * @tparam S2 second type in OSGi service registry
   * @tparam S3 third type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest, S2 >: S: ClassManifest, S3 >: S: ClassManifest]: ServiceRegistration[S] = {
    providesService[S1, S2, S3]()
  }

  /**
   * Registers the service under the specified types with the given service properties.
   *
   * @param properties service properties
   * @tparam S1 first type in OSGi service registry
   * @tparam S2 second type in OSGi service registry
   * @tparam S3 third type in OSGi service registry
   * @return service registration
   */
  def providesService[S1 >: S: ClassManifest, S2 >: S: ClassManifest, S3 >: S: ClassManifest]
      (properties: Map[String, Any]): ServiceRegistration[S] = {

    providesService[S1, S2, S3](properties.toSeq: _*)
  }

  protected def providesServiceInternal(manifests: List[ClassManifest[_]], properties: Seq[(String, Any)]): ServiceRegistration[S] = {
    val sp = new ServiceProviderCapsule(manifests, properties, bundleContext, service)
    capsuleContext.addCapsule(sp)
    sp.reg
  }
}