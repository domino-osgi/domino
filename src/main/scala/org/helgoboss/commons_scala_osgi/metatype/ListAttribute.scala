package org.helgoboss.commons_scala_osgi.metatype

object ListAttribute {
  def apply[T: ClassManifest](
    id: String,
    default: Option[List[T]] = None,
    name: String = null,
    description: String = null,
    options: Option[Map[T, String]] = None) = {

    val idParam = id
    val nameParam = name
    val descriptionParam = description
    val optionsParam = options

    new ListAttributeDefinition[T] {
      val id = idParam
      val name = Option(nameParam) getOrElse id
      val description = Option(descriptionParam) getOrElse name
      val options = optionsParam
      val defaultValue = default
      val sizeLimit = None
      def validate(value: T) = ValidationResult.NotValidated
    }
  }
}