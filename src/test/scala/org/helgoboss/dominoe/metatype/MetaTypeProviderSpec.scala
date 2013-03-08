package org.helgoboss.dominoe.metatype

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.framework.BundleContext
import java.util.Locale

@RunWith(classOf[JUnitRunner])
class MetaTypeProviderSpec extends WordSpec with ShouldMatchers {
        
    "MetaTypeProvider" should {
        "be definable in a Scalaish way" in {
            new MetaTypeProvider {
                def locales = List(new Locale("DE"))
                def getObjectClassDefinition(id: String, locale: Option[Locale]) = {
                    new ObjectClassDefinition {
                        def requiredAttributeDefinitions = List (
                            new ListAttributeDefinition[String] {
                                def sizeLimit = None
                                def defaultValue = Some(List("apples"))
                                def description = name
                                def id = "allowedFruits"
                                def name = "Allowed fruits"
                                def options = Some(Map(
                                    "apples" -> "Apples",
                                    "bananas" -> "Bananas"
                                ))
                                def validate(value: String) = ValidationResult.NotValidated
                            },
                            new ElementaryAttributeDefinition[Int] {
                                import ValidationResult._
                                def description = name
                                def defaultValue = None
                                def id = "size"
                                def name = "Size"
                                def options = None
                                def validate(value: Int) = if (value < 0) Invalid("Value has to be positive") else Valid
                            }
                        )
                        def optionalAttributeDefinitions = Nil
                        def description = ""
                        def getIcon(size: Int) = None
                        def id = "efed.de"
                        def name = "wdfe"
                    }
                }
            }
        }
        
        "be definable in a very terse and less flexible way" in {
            ObjectClass(
                id = "efed.de", 
                requiredAttributes = List (
                    ListAttribute[String](id = "allowedFruits", default = Some(List("apples")), options = Some(Map("apples" -> "Apples", "bananas" -> "Bananas"))),
                    ElementaryAttribute[Int](id = "size")
                )
            ).toMetaTypeProvider
        }
    }
    
    "An ObjectClassDefinition" should {
        "be able to output default configuration" in {
            val generatedDefaultConf = ObjectClass(
                id = "efed.de", 
                requiredAttributes = List (
                    ListAttribute[String](id = "allowedFruits", default = Some(List("apples")), options = Some(Map("apples" -> "Apples", "bananas" -> "Bananas"))),
                    ElementaryAttribute[Int](id = "size")
                )
            ).defaultConfig
            
            assert(generatedDefaultConf === Map("allowedFruits" -> List("apples")))
        }
    }
    
}