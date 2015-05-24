package domino.scala_osgi_metatype.interfaces

import java.io.InputStream

/**
 * Description for the data type information of an objectclass. Interface modeled after
 * [[org.osgi.service.metatype.ObjectClassDefinition]].
 */
trait ObjectClassDefinition {
  /**
   * Returns the attribute definitions of required attributes for this object class.
   */
  def requiredAttributeDefinitions: Traversable[AttributeDefinition[_]]

  /**
   * Returns the attribute definitions of optional attributes for this object class.
   */
  def optionalAttributeDefinitions: Traversable[AttributeDefinition[_]]

  /**
   * Returns a description of this object class.
   */
  def description: String

  /**
   * Returns an InputStream object that can be used to create an icon from.
   */
  def getIcon(size: Int): Option[InputStream]

  /**
   * Returns the id of this object class.
   */
  def id: String

  /**
   * Returns the name of this object class.
   */
  def name: String
}