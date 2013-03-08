package org.helgoboss.commons_scala_osgi

import org.osgi.framework.{ BundleContext, ServiceRegistration }
import org.helgoboss.module_support._

object RichService {
  val CompleteTypesExpressionKey = "completeTypesExpression"

  /**
   * Create an expression for the complete type including generic type parameters, only if one of the manifests
   * has type parameters, otherwise returns None.
   *
   * Input:
   * - Map[String, Map[String, Integer]]
   * - List[Number]
   * - String
   *
   * Result: ";Map[String, Map[String, Integer]];List[Number];String;" (package names omitted).
   *
   * Note that a semicolon instead of a comma is used to separate the types.
   */
  def createCompleteTypesExpression(classManifests: List[ClassManifest[_]]): Option[String] = {
    if (classManifests exists { _.typeArguments.size > 0 }) {
      val sep = ";"
      Some(classManifests.mkString(sep, sep, sep))
    } else {
      None
    }
  }

  def createCompleteTypeExpressionFilter(cm: ClassManifest[_]): Option[String] = {
    val expression = createCompleteTypesExpression(List(cm))
    expression match {
      case Some(e) =>
        if (e.isEmpty) {
          None
        } else {
          Some("(" + CompleteTypesExpressionKey + "=*" + e + "*)")
        }
      case None =>
        None
    }
  }

  def linkFiltersWithAnd(filterOne: Option[String], filterTwo: Option[String]): Option[String] = {
    // TODO Do this more elegantly
    filterOne match {
      case Some(f1) =>
        filterTwo match {
          case Some(f2) =>
            Some("(&" + f1 + f2 + ")")

          case None =>
            filterOne
        }

      case None =>
        filterTwo
    }
  }
}

class RichService(service: Any, moduleContext: ModuleContext, bundleContext: BundleContext) {
  import RichService._

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
    val sp = new ServiceProvider(manifests, properties)
    moduleContext.addModule(sp)
    sp.reg
  }

  private class ServiceProvider(manifests: List[ClassManifest[_]], properties: Seq[(String, Any)]) extends Module {
    var reg: ServiceRegistration = _

    def start() {
      // Create OBJECTCLASS array
      val interfaceArray = manifests map { _.erasure.getName } toArray

      // Add complete types expression to properties if necessary
      val extendedProperties = createCompleteTypesExpression(manifests) match {
        case Some(e) =>
          (CompleteTypesExpressionKey -> e) +: properties

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
}