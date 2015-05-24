package domino.scala_osgi_metatype.interfaces

/**
 * An interface to describe an attribute. Interface modeled after
 * [[org.osgi.service.metatype.AttributeDefinition]].
 */
sealed trait AttributeDefinition[T] {
  /**
   * Returns a description of this attribute.
   */
  def description: String

  /**
   * Unique identity for this attribute.
   */
  def id: String

  /**
   * Gets the name of the attribute.
   */
  def name: String

  /**
   * Return a list of possible option labels and values that this attribute can take or `Nil`.
   */
  def options: Traversable[(String, T)]

  /**
   * Validates an attribute.
   */
  def validate(value: T): ValidationResult

  /**
   * Returns a default for this attribute.
   */
  def defaultValue: Option[_]

  /**
   * Returns the value class.
   */
  def valueType: Class[_]
}

/**
 * An interface to describe a attribute with a single value. Interface modeled after
 * [[org.osgi.service.metatype.AttributeDefinition]] with a cardinality of 0.
 */
trait ListAttributeDefinition[T] extends AttributeDefinition[T] {
  /**
   * If given, denotes the maximum number of allowed values.
   */
  def sizeLimit: Option[Int]

  def defaultValue: Option[Traversable[T]]
}

/**
 * An interface to describe a attribute with a single value. Interface modeled after
 * [[org.osgi.service.metatype.AttributeDefinition]] with a cardinality of 0.
 */
trait ElementaryAttributeDefinition[T] extends AttributeDefinition[T] {
  def defaultValue: Option[T]
}