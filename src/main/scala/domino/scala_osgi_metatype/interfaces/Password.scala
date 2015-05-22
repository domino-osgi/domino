package domino.scala_osgi_metatype.interfaces

/**
 * A value type applicable to [[AttributeDefinition]]. Maps to
 * [[org.osgi.service.metatype.AttributeDefinition.PASSWORD]].
 *
 * @constructor Creates a password value.
 * @param password The wrapped password
 */
case class Password(password: String)
