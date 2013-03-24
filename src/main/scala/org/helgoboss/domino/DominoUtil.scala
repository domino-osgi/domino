package org.helgoboss.domino

import java.util.{Vector, Hashtable, Dictionary}
import org.osgi.framework.Constants

/**
 * Contains utility methods used throughout Domino.
 */
object DominoUtil {
  /**
   * The OSGi service property key for saving the generic types expression.
   */
  val GenericsExpressionKey = "completeTypesExpression"
  
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
   * Converts the given Java Dictionary to a Scala Map.
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
   * Creates an expression which describes the complete type information of the given class manifests
   * including generic type parameters. If none of the given manifests contains type parameters,
   * it returns `None`.
   *
   * The resulting expression is supposed to be registered as OSGi service property so it can be
   * used as a criteria in a service query.
   *
   * Example input:
   *  - `Map[String, Map[String, Integer]]`
   *  - `List[Number]`
   *  - `String`
   *    
   * Example result: `";Map[String, Map[String, Integer]];List[Number];String;"` (package names omitted)
   *    
   * @note A semicolon is used instead of a comma to separate the types.
   *
   * @param classManifests Manifests which contain the type information
   * @return types expression if generic type parameters are used
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
          Some("(" + GenericsExpressionKey + "=*" + e + "*)")
        }
      case None =>
        None
    }
  }

  /**
   * Creates a filter criteria expression which matches the given generic type and the given custom filter.
   * Doesn't include the main `OBJECTCLASS` filter criteria. If no custom filter is given and
   * generic types are not used, returns `None`.
   *
   * @param cm Type information
   * @param customFilter A custom filter expression
   */
  def createGenericsAndCustomFilter(cm: ClassManifest[_], customFilter: String): Option[String] = {
    // Create the generic type filter criteria
    val completeTypeExpressionFilter = DominoUtil.createGenericsFilter(cm)

    // Link it with the custom filter
    DominoUtil.linkFiltersWithAnd(completeTypeExpressionFilter, Option(customFilter))
  }

  /**
   * Creates a filter criteria expression which matches the given main type, the generic type and the given custom filter.
   * Thus, it includes the main `OBJECTCLASS` filter criteria.
   *
   * @param cm Type information
   * @param customFilter A custom filter expression
   */
  def createCompleteFilter(cm: ClassManifest[_], customFilter: String): String = {
    // Create object class and generics and custom filter
    val objectClassFilter = createObjectClassFilter(cm.erasure)
    val genericsAndCustomFilter = createGenericsAndCustomFilter(cm, customFilter)

    // Combine
    DominoUtil.linkFiltersWithAnd(Some(objectClassFilter), genericsAndCustomFilter).get
  }

  /**
   * Creates an `OBJECTCLASS` filter for the given class.
   */
  def createObjectClassFilter(clazz: Class[_]): String = {
    "(" + Constants.OBJECTCLASS + "=" + clazz.getName + ")"
  }


  /**
   * Links to filter expressions with a logical AND if both are given, otherwise returns just one of it.
   *
   * @param filterOne First filter
   * @param filterTwo Second filter
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
