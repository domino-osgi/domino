package org.helgoboss.dominoe

import java.util.{Vector, Hashtable, Dictionary}


object DominoeUtil {
  def convertToDictionary(map: Map[String, Any]): Dictionary[String, AnyRef] = {
    val table = new Hashtable[String, AnyRef]
    map.foreach {
      case (key, value) =>
        table.put(key, value.asInstanceOf[AnyRef])
    }
    table
  }

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
