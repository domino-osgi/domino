package domino.scala_osgi_metatype.interfaces

/**
 * Represents a validation result of [[AttributeDefinition]]. Possible values are defined in the companion object.
 */
sealed trait ValidationResult

/**
 * Contains the possible validation results.
 */
object ValidationResult {
  /**
   * Returned if no validation has been done.
   */
  object NotValidated extends ValidationResult

  /**
   * Returned if the configuration value is valid.
   */
  object Valid extends ValidationResult

  /**
   * Returned if the configuration value is invalid.
   */
  case class Invalid(reason: String) extends ValidationResult
}
