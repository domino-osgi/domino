package org.helgoboss.dominoe.metatype

object ElementaryAttribute {
  def apply[T: ClassManifest](
    id: String,
    default: Option[T] = None,
    name: String = null,
    description: String = null,
    options: Option[Map[T, String]] = None) = {

    val idParam = id
    val nameParam = name
    val descriptionParam = description
    val optionsParam = options

    new ElementaryAttributeDefinition[T] {
      val id = idParam
      val name = Option(nameParam) getOrElse id
      val description = Option(descriptionParam) getOrElse name
      val options = optionsParam
      val defaultValue = default
      def validate(value: T) = ValidationResult.NotValidated
    }
  }
}