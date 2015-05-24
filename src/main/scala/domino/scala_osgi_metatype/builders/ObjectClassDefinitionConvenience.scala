package domino.scala_osgi_metatype.builders

import domino.scala_osgi_metatype.interfaces.ObjectClassDefinition

/**
 * Adds some convenience methods to the given object class definition.
 */
trait ObjectClassDefinitionConvenience {
  /**
   * Object class definition.
   */
  protected def definition: ObjectClassDefinition

  /**
   * Returns both the attribute definitions of required and optional attributes for this object class definition.
   */
  lazy val allAttributeDefinitions = definition.requiredAttributeDefinitions ++ definition.optionalAttributeDefinitions

  /**
   * Builds a configuration map containing all the default values.
   *
   * Ideal for overlaying a default configuration with an actual one:
   * {{{
   *   val finalConfig = actualConfig ++ objectClass.defaultConfig
   * }}}
   */
  lazy val defaultConfig = {
    allAttributeDefinitions flatMap { definition =>
      definition.defaultValue map { value =>
        definition.id -> value
      }
    } toMap
  }
}
