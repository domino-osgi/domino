package domino

import java.util.{ Vector, Hashtable, Dictionary }
import org.osgi.framework.Constants
import scala.reflect.runtime.universe._
import org.osgi.framework.Filter
import org.osgi.framework.FrameworkUtil

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
   * Creates an expression which describes the complete type information of the given types
   * including generic type parameters. If none of the given types contains type parameters,
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
   * @param types Type objects which might contain information about generic type arguments
   * @return types expression if generic type parameters are used
   */
  def createGenericsExpression(types: Traversable[Type]): Option[String] = {
    if (types exists { hasTypeArguments }) {
      val sep = ";"
      Some(types.mkString(sep, sep, sep))
    } else {
      None
    }
  }

  /**
   * Returns whether the given type tag has type parameters.
   */
  def hasTypeArguments(tpe: Type): Boolean = {
    !tpe.asInstanceOf[TypeRefApi].args.isEmpty
  }

  /**
   * Returns the qualified name of the given type.
   */
  def getFullTypeName(tpe: Type): String = {
    tpe.asInstanceOf[TypeRefApi].typeSymbol.fullName
  }

  /**
   * Creates a filter expression that would match the given type with its generic type parameters.
   * Uses wildcards because the service can be registered under several types. That would result
   * in several generic type expressions separated by semicolon.
   *
   * If no generic type is used in the type, returns `None`.
   */
  def createGenericsFilter(tpe: Type): Option[Filter] = {
    val expression = createGenericsExpression(List(tpe))
    expression.flatMap { e =>
      if (e.isEmpty) {
        None
      } else {
        Some(FrameworkUtil.createFilter(s"(${GenericsExpressionKey}=*${e}*)"))
      }
    }
  }

  /**
   * Creates a filter criteria expression which matches the given type and the given custom filter.
   * Doesn't include the main `OBJECTCLASS` filter criteria. If no custom filter is given and
   * generic types are not used, returns `None`.
   *
   * @param tpe Type information
   * @param customFilter A custom filter expression
   */
  def createGenericsAndCustomFilter(tpe: Type, customFilter: Filter): Option[Filter] = {
    // Create the generic type filter criteria
    val completeTypeExpressionFilter = createGenericsFilter(tpe)

    // Link it with the custom filter
    linkFiltersWithAnd(completeTypeExpressionFilter, Option(customFilter))
  }

  /**
   * Creates a filter criteria expression which matches the given main type, the generic type and the given custom filter.
   * Thus, it includes the main `OBJECTCLASS` filter criteria.
   *
   * @param tpe Type information
   * @param customFilter A custom filter expression
   */
  def createCompleteFilter(tpe: Type, customFilter: Filter): Filter = {
    // Create object class and generics and custom filter
    val className = getFullTypeName(tpe)
    val objectClassFilter = createObjectClassFilter(className)
    val genericsAndCustomFilter = createGenericsAndCustomFilter(tpe, customFilter)

    // Combine
    linkFiltersWithAnd(Some(objectClassFilter), genericsAndCustomFilter).get
  }

  /**
   * Creates an `OBJECTCLASS` filter for the given class.
   */
  def createObjectClassFilter(typeName: String): Filter = {
    FrameworkUtil.createFilter(s"(${Constants.OBJECTCLASS}=${typeName})")
  }

  /**
   * Links to filter expressions with a logical AND if both are given, otherwise returns just one of it.
   *
   * @param filterOne First filter
   * @param filterTwo Second filter
   * @return result
   */
  def linkFiltersWithAnd(filterOne: Option[Filter], filterTwo: Option[Filter]): Option[Filter] = {
    // TODO Do this more elegantly
    filterOne match {
      case Some(f1) =>
        filterTwo match {
          case Some(f2) =>
            Some(FrameworkUtil.createFilter(s"(&${f1.toString()}${f2.toString()})"))

          case None =>
            filterOne
        }

      case None =>
        filterTwo
    }
  }
}
