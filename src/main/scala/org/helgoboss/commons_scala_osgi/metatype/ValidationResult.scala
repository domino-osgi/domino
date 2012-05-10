package org.helgoboss.commons_scala_osgi.metatype

sealed trait ValidationResult

object ValidationResult {
    object NotValidated extends ValidationResult
    object Valid extends ValidationResult
    case class Invalid(val reason: String) extends ValidationResult
}