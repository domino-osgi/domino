package org.helgoboss.commons_scala_osgi.metatype

import java.io.InputStream
import org.osgi.service.metatype.{ObjectClassDefinition => JObjectClassDefinition, AttributeDefinition => JAttributeDefinition}
import java.util.Locale

object ObjectClassDefinition {
    /**
     * An ObjectClassDefinition can implicitly serve as a MetaTypeProvider
     */
    implicit def objectClassDefinition2MetaTypeProvider(d: ObjectClassDefinition) = d.toMetaTypeProvider
    
    /**
     * Implicit conversion to its Java counterpart.
     */
    implicit def objectClassDefinition2JObjectClassDefinition(d: ObjectClassDefinition) = d.osgiCompliant
}

/** 
 * Represents an object class definition as defined by the OSGi meta-type specification. It is a pure Scalafication
 * of the equally-named Java interface in the OSGi API. I decided not to implement the Java interface because I
 * wanted a pure Scala API, prevent name clashes and prevent confusion about what method to call. Scala Swing 
 * does it the same way, I guess for the same reasons. For convenience, however, implicit conversions to its
 * Java counterparts exist in the companion object.
 */ 
trait ObjectClassDefinition {
    def requiredAttributeDefinitions: List[AttributeDefinition[_]]
    def optionalAttributeDefinitions: List[AttributeDefinition[_]]
    final def allAttributeDefinitions = requiredAttributeDefinitions ++ optionalAttributeDefinitions
    def description: String
    def getIcon(size: Int): Option[InputStream]
    def id: String
    def name: String
    
    lazy val toMetaTypeProvider = new MetaTypeProvider {
        val locales = Nil
        def getObjectClassDefinition(id: String, locale: Option[Locale]) = ObjectClassDefinition.this
    }
    
    def defaultConfig = {
        val idValuePairs = allAttributeDefinitions.flatMap { d =>
            d.defaultValue match {
                case Some(v) => Some(d.id -> v)
                case None => None
            }
        }
        idValuePairs.toMap
    }
    
    lazy val osgiCompliant = new JObjectClassDefinition {
        def getAttributeDefinitions(filter: Int): Array[JAttributeDefinition] = {
            import JObjectClassDefinition._
            val list = filter match {
                case REQUIRED => requiredAttributeDefinitions
                case OPTIONAL => optionalAttributeDefinitions
                case ALL => requiredAttributeDefinitions ++ optionalAttributeDefinitions
            }
            if (list.isEmpty) {
                null
            } else {
                list.map(_.osgiCompliant).toArray
            }
        }
        def getDescription = description
        def getIcon(size: Int) = ObjectClassDefinition.this.getIcon(size) match {
            case Some(is) => is
            case None => null
        }
        def getID = id
        def getName = name
    }
}