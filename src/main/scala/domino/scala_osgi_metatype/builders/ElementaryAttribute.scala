package domino.scala_osgi_metatype.builders

import domino.scala_osgi_metatype.interfaces.{ValidationResult, ElementaryAttributeDefinition}
import reflect.ClassTag
import reflect.classTag

/**
 * Convenient builder for elementary attribute definitions without advanced validation
 * (type validation is done).
 *
 * == Examples ==
 *
 * {{{
 * // Minimum
 * ElementaryAttribute[Int](id = "size")
 *
 * // Maximum
 * ElementaryAttribute[Int](
 *   id = "size",
 *   name = "Size",
 *   description = "Size of the container",
 *   default = 5,
 *   options = List(
 *     "Small" -> 2,
 *     "Medium" -> 5,
 *     "Large" -> 10
 * )
 * }}}
 */
object ElementaryAttribute {

  /**
   * Builds an elementary attribute definition.
   *
   * @param id Unique identity for the attribute
   * @param name Optional name of the attribute
   * @param description Optional description of the attribute
   * @param default Optional default value for the attribute
   * @param options Optional list of possible option labels and values that the attribute can take
   * @tparam T Attribute type
   */
  def apply[T: ClassTag](
      id: String,
      name: String = null,
      description: String = "",
      default: Option[T] = None,
      options: Traversable[(String, T)] = Nil) = {

    // Rename parameters so we can use them in the anonymous class without becoming recursive
    val idParam = id
    val nameParam = name
    val descriptionParam = description
    val optionsParam = options

    // Build the definition
    new ElementaryAttributeDefinition[T] {
      val id = idParam
      val name = Option(nameParam) getOrElse id
      val description = descriptionParam
      val options = optionsParam
      val defaultValue = default
      val valueType = classTag[T].runtimeClass
      def validate(value: T) = ValidationResult.NotValidated
    }
  }
}