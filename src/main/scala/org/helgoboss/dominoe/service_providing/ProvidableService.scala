package org.helgoboss.dominoe.service_providing

import org.osgi.framework.{ BundleContext, ServiceRegistration }
import org.helgoboss.module_support._

class ProvidableService(service: Any, moduleContext: ModuleContext, bundleContext: BundleContext) {
  def providesService[S <: AnyRef: ClassManifest](properties: (String, Any)*): ServiceRegistration = {
    providesServiceInternal(
      List(classManifest[S]),
      properties)
  }

  def providesService[S <: AnyRef: ClassManifest]: ServiceRegistration = {
    providesService[S]()
  }

  def providesService[S <: AnyRef: ClassManifest](properties: Map[String, Any]): ServiceRegistration = {
    providesService[S](properties.toSeq: _*)
  }

  def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest](properties: (String, Any)*): ServiceRegistration = {
    providesServiceInternal(
      List(classManifest[S1], classManifest[S2]),
      properties)
  }

  def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest]: ServiceRegistration = {
    providesService[S1, S2]()
  }

  def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest](properties: Map[String, Any]): ServiceRegistration = {
    providesService[S1, S2](properties.toSeq: _*)
  }

  def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest](properties: (String, Any)*): ServiceRegistration = {
    providesServiceInternal(
      List(classManifest[S1], classManifest[S2], classManifest[S3]),
      properties)
  }

  def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest]: ServiceRegistration = {
    providesService[S1, S2, S3]()
  }

  def providesService[S1 <: AnyRef: ClassManifest, S2 <: AnyRef: ClassManifest, S3 <: AnyRef: ClassManifest](properties: Map[String, Any]): ServiceRegistration = {
    providesService[S1, S2, S3](properties.toSeq: _*)
  }

  private def providesServiceInternal(manifests: List[ClassManifest[_]], properties: Seq[(String, Any)]): ServiceRegistration = {
    val sp = new ServiceProviderModule(manifests, properties, bundleContext, service)
    moduleContext.addModule(sp)
    sp.reg
  }
}