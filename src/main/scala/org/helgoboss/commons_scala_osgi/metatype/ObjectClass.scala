package org.helgoboss.commons_scala_osgi.metatype

/**
 * Builder for easily defining an ObjectClassDefinition.
 *
 * I've chosen not to create a named case class extending ObjectClassDefinition because it would cause name/type conflicts.
 * Moreover, the named class wouldn't add any benefits and would be just another class name to wonder about.
 */
object ObjectClass {
    def apply(
            id: String,
            name: String = null,
            description: String = null,
            requiredAttributes: List[AttributeDefinition[_]] = Nil,
            optionalAttributes: List[AttributeDefinition[_]] = Nil) = {
        
        /* Rename parameters in order to avoid name clashes. We don't rename the parameters because they are part of the 
         * public interface as they shall be used as named parameters.
         */
        val idParam = id
        val nameParam = name
        val descriptionParam = description
        
        new ObjectClassDefinition {
            val id = idParam
            val name = Option(nameParam) getOrElse id
            val description = Option(descriptionParam) getOrElse name
            val requiredAttributeDefinitions = requiredAttributes
            val optionalAttributeDefinitions = optionalAttributes
            def getIcon(size: Int) = None
        }
    }
}