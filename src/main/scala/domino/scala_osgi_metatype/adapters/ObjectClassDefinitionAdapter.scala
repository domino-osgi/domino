package domino.scala_osgi_metatype.adapters

import org.osgi.service.metatype.{ObjectClassDefinition => JObjectClassDefinition, AttributeDefinition => JAttributeDefinition}
import domino.scala_osgi_metatype.interfaces.{ListAttributeDefinition, ElementaryAttributeDefinition, ObjectClassDefinition}

/**
 * Provides the given Scala object class definition as an OSGi-compliant object class definition.
 *
 * @constructor Creates an adapter for the given definition.
 * @param delegate Scala object class definition
 */
class ObjectClassDefinitionAdapter(delegate: ObjectClassDefinition) extends JObjectClassDefinition {
  def getAttributeDefinitions(filter: Int): Array[JAttributeDefinition] = {
    import JObjectClassDefinition._

    val list = filter match {
      case REQUIRED => delegate.requiredAttributeDefinitions
      case OPTIONAL => delegate.optionalAttributeDefinitions
      case ALL => delegate.requiredAttributeDefinitions ++ delegate.optionalAttributeDefinitions
    }

    if (list.isEmpty) {
      null
    } else {
      list.map {
        case ed: ElementaryAttributeDefinition[_] => new ElementaryAttributeDefinitionAdapter(ed)
        case ld: ListAttributeDefinition[_] => new ListAttributeDefinitionAdapter(ld)
      }.toArray
    }
  }

  def getDescription = delegate.description

  def getIcon(size: Int) = delegate.getIcon(size).orNull

  def getID = delegate.id

  def getName = delegate.name
}
