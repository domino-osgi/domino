package org.helgoboss.commons_scala_osgi.metatype

abstract class ElementaryAttributeDefinition[T: ClassManifest] extends AttributeDefinition[T] {
    def defaultValue: Option[T]
    def validate(value: T): ValidationResult
    
    lazy val osgiCompliant = new OsgiCompliantAttributeDefinition {
        def getCardinality = 0
        
        def getDefaultValue = if (defaultValue.isEmpty) null else defaultValue.map(_.toString).toArray
    }
}