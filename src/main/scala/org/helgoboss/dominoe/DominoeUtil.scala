package org.helgoboss.dominoe

import java.util.{Vector, Hashtable, Dictionary}
import org.osgi.framework.Constants

/**
 * Contains some utility methods used in Dominoe.
 */
object DominoeUtil {
  /**
   * Converts the given Scala Map to a Java Dictionary.
   */
  def convertToDictionary(map: Map[String, Any]): Dictionary[String, AnyRef] = {
    val table = new Hashtable[String, AnyRef]
    map.foreach {
      case (key, value) =>
        table.put(key, value.asInstanceOf[AnyRef])
    }
    table
  }

  /**
   * Converts the given dictionary to a Scala Map.
   */
  def convertToMap(dictionary: Dictionary[_, _]): Map[String, Any] = {
    val map = new collection.mutable.HashMap[String, Any]
    import collection.JavaConversions._
    dictionary.keys.foreach { key =>
      val jValue = dictionary.get(key)
      val value = jValue match {
        case v: Vector[_] => v.toList
        case a: Array[_] => a.toList
        case _ => jValue
      }
      map(key.asInstanceOf[String]) = value.asInstanceOf[Any]
    }
    map.toMap
  }

  /**
   * This key is the one used for describing the generic type in the service properties of a
   * registered service.
   */
  val CompleteTypesExpressionKey = "completeTypesExpression"

  /**
   * Creates an expression for the complete type including generic type parameters, only if one of the manifests
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
  def createGenericsExpression(classManifests: List[ClassManifest[_]]): Option[String] = {
    if (classManifests exists { _.typeArguments.size > 0 }) {
      val sep = ";"
      Some(classManifests.mkString(sep, sep, sep))
    } else {
      None
    }
  }

  /**
   * Creates a filter expression that would match the given type with its generic type parameters.
   * Uses wildcards because the service can be registered under several types. That would result
   * in several generic type expressions separated by semicolon.
   *
   * If no generic type is used in the manifest, returns `None`.
   */
  def createGenericsFilter(cm: ClassManifest[_]): Option[String] = {
    val expression = createGenericsExpression(List(cm))
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

  /**
   * Creates a filter criteria which matches the given generic type and the given custom filter.
   * Doesn't include the main OBJECTCLASS filter criteria. If no custom filter is given and
   * generic types are not used, returns `None`.
   */
  def createGenericsAndCustomFilter(cm: ClassManifest[_], customFilter: String): Option[String] = {
    // Create the generic type filter criteria
    val completeTypeExpressionFilter = DominoeUtil.createGenericsFilter(cm)

    // Link it with the custom filter
    DominoeUtil.linkFiltersWithAnd(completeTypeExpressionFilter, Option(customFilter))
  }

  /**
   * Creates a filter criteria which matches the given main type, the generic type and the given custom filter.
   * Thus, it includes the main OBJECTCLASS filter criteria.
   */
  def createCompleteFilter(cm: ClassManifest[_], customFilter: String): String = {
    // Create object class and generics and custom filter
    val objectClassFilter = createObjectClassFilter(cm.erasure)
    val genericsAndCustomFilter = createGenericsAndCustomFilter(cm, customFilter)

    // Combine
    DominoeUtil.linkFiltersWithAnd(Some(objectClassFilter), genericsAndCustomFilter).get
  }

  /**
   * Creates an OBJECTCLASS filter for the given class.
   */
  def createObjectClassFilter(clazz: Class[_]): String = {
    "(" + Constants.OBJECTCLASS + "=" + clazz.getName + ")"
  }


  /**
   * Links to filters with a logical AND if both are given, otherwise returns just one.
   *
   * @param filterOne first filter
   * @param filterTwo second filter
   * @return result
   */
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
