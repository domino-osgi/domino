package domino.scala_osgi_metatype.adapters

import domino.scala_osgi_metatype.interfaces.ElementaryAttributeDefinition



/**
 * Provides the given Scala elementary attribute definition as an OSGi-compliant attribute definition.
 *
 * @constructor Creates an adapter for the given definition.
 * @param delegate Scala elementary attribute definition
 */
class ElementaryAttributeDefinitionAdapter[T](delegate: ElementaryAttributeDefinition[T])
  extends AttributeDefinitionAdapter[T](delegate) {

  def getCardinality = 0

  lazy val getDefaultValue = {
    if (delegate.defaultValue.isEmpty)
      null
    else
      delegate.defaultValue map { _.toString } toArray
  }
}