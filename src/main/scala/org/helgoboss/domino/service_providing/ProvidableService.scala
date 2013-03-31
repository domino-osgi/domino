package org.helgoboss.domino.service_providing

import org.osgi.framework.{ BundleContext, ServiceRegistration }
import org.helgoboss.capsule._
import scala.reflect.runtime.universe._
import reflect.ClassTag

/**
 * Offers methods to register the wrapped service as OSGi service.
 *
 * @param service Wrapped service
 * @param capsuleContext Dependency
 * @param bundleContext Dependency
 * @tparam S Service type
 */
class ProvidableService[S](service: S, capsuleContext: CapsuleContext, bundleContext: BundleContext) {
  /**
   * Registers the service under the specified type with the given service properties.
   *
   * @param properties Service properties
   * @tparam S1 Type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag](properties: (String, Any)*): ServiceRegistration[S] = {
    providesServiceInternal(
      List(typeTag[S1]),
      properties
    )
  }

  /**
   * Registers the service under the specified type and without service properties.
   *
   * @tparam S1 Type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag]: ServiceRegistration[S] = {
    providesService[S1]()
  }

  /**
   * Registers the service under the specified type with the given service properties.
   *
   * @param properties Service properties
   * @tparam S1 Type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag](properties: Map[String, Any]): ServiceRegistration[S] = {
    providesService[S1](properties.toSeq: _*)
  }

  /**
   * Registers the service under the specified types with the given service properties.
   *
   * @param properties Service properties
   * @tparam S1 First type in OSGi service registry
   * @tparam S2 Second type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag, S2 >: S: TypeTag: ClassTag](properties: (String, Any)*): ServiceRegistration[S] = {
    providesServiceInternal(
      List(typeTag[S1], typeTag[S2]),
      properties
    )
  }

  /**
   * Registers the service under the specified types without service properties.
   *
   * @tparam S1 First type in OSGi service registry
   * @tparam S2 Second type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag, S2 >: S: TypeTag: ClassTag]: ServiceRegistration[S] = {
    providesService[S1, S2]()
  }

  /**
   * Registers the service under the specified types with the given service properties.
   *
   * @param properties service properties
   * @tparam S1 First type in OSGi service registry
   * @tparam S2 Second type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag, S2 >: S: TypeTag: ClassTag]
      (properties: Map[String, Any]): ServiceRegistration[S] = {

    providesService[S1, S2](properties.toSeq: _*)
  }

  /**
   * Registers the service under the specified types with the given service properties.
   *
   * @param properties Service properties
   * @tparam S1 First type in OSGi service registry
   * @tparam S2 Second type in OSGi service registry
   * @tparam S3 Third type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag, S2 >: S: TypeTag: ClassTag, S3 >: S: TypeTag: ClassTag]
      (properties: (String, Any)*): ServiceRegistration[S] = {

    providesServiceInternal(
      List(typeTag[S1], typeTag[S2], typeTag[S3]),
      properties
    )
  }

  /**
   * Registers the service under the specified types without service properties.
   *
   * @tparam S1 First type in OSGi service registry
   * @tparam S2 Second type in OSGi service registry
   * @tparam S3 Third type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag, S2 >: S: TypeTag: ClassTag, S3 >: S: TypeTag: ClassTag]: ServiceRegistration[S] = {
    providesService[S1, S2, S3]()
  }

  /**
   * Registers the service under the specified types with the given service properties.
   *
   * @param properties Service properties
   * @tparam S1 First type in OSGi service registry
   * @tparam S2 Second type in OSGi service registry
   * @tparam S3 Third type in OSGi service registry
   * @return Service registration
   */
  def providesService[S1 >: S: TypeTag: ClassTag, S2 >: S: TypeTag: ClassTag, S3 >: S: TypeTag: ClassTag]
      (properties: Map[String, Any]): ServiceRegistration[S] = {

    providesService[S1, S2, S3](properties.toSeq: _*)
  }

  protected def providesServiceInternal(typeTags: Traversable[TypeTag[_]], properties: Seq[(String, Any)]): ServiceRegistration[S] = {
    val types = typeTags.view map { _.tpe }
    val sp = new ServiceProviderCapsule(types, properties, bundleContext, service)
    capsuleContext.addCapsule(sp)
    sp.reg
  }
}