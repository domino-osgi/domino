package org.helgoboss.dominoe.metatype

sealed trait ValidationResult

object ValidationResult {
  object NotValidated extends ValidationResult
  object Valid extends ValidationResult
  case class Invalid(reason: String) extends ValidationResult
}