package domino.scala_osgi_metatype.adapters

import domino.scala_osgi_metatype.interfaces.ListAttributeDefinition

/**
 * Provides the given Scala list attribute definition as an OSGi-compliant attribute definition.
 *
 * @constructor Creates an adapter for the given definition.
 * @param delegate Scala list attribute definition
 */
class ListAttributeDefinitionAdapter[T](delegate: ListAttributeDefinition[T])
    extends AttributeDefinitionAdapter[T](delegate) {

  def getCardinality = delegate.sizeLimit match {
    case Some(x) => x
    case None => Int.MaxValue
  }

  lazy val getDefaultValue = {
    delegate.defaultValue map { v =>
      v.map { _.toString } toArray
    } orNull
  }
}