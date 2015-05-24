package domino.scala_osgi_metatype.builders

import domino.scala_osgi_metatype.interfaces.{ValidationResult, ListAttributeDefinition}
import reflect.ClassTag
import reflect.classTag

/**
 * Convenient builder for list attribute definitions without advanced validation (type validation is done).
 *
 * == Examples ==
 *
 * {{{
 * // Minimum
 * ListAttribute[String](id = "allowedFruits")
 *
 * // Maximum
 * ListAttribute[String](
 *   id = "allowedFruits",
 *   name = "Allowed fruits",
 *   description = "These fruits are allowed",
 *   default = List("apple", "orange"),
 *   options = List(
 *     "Apple" -> "apple",
 *     "Orange" -> "orange",
 *     "Plum" -> "plum",
 *     "Banana" -> "banana"
 * )
 * }}}
 */
object ListAttribute {
  /**
   * Builds a list attribute definition.
   *
   * @param id Unique identity for the attribute
   * @param name Optional name of the attribute
   * @param description Optional description of the attribute
   * @param default Optional default for the attribute
   * @param options Optional list of possible option labels and values that the attribute can take
   * @param sizeLimit Optional maximum number of allowed values.
   * @tparam T Type of the elements
   */
  def apply[T: ClassTag](
    id: String,
    name: String = null,
    description: String = "",
    default: Traversable[T] = null,
    options: Traversable[(String, T)] = Nil,
    sizeLimit: java.lang.Integer = null) = {

    // Rename parameters so we can use them in the anonymous class without becoming recursive
    val idParam = id
    val nameParam = name
    val descriptionParam = description
    val optionsParam = options
    val sizeLimitParam = sizeLimit

    // Build the definition
    new ListAttributeDefinition[T] {
      val id = idParam
      val name = Option(nameParam) getOrElse id
      val description = descriptionParam
      val options = optionsParam
      val defaultValue = Option(default)
      val sizeLimit = Option(sizeLimitParam) map { _.toInt }
      val valueType = classTag[T].runtimeClass
      def validate(value: T) = ValidationResult.NotValidated
    }
  }
}