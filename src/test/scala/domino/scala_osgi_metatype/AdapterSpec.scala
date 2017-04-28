package domino.scala_osgi_metatype

import adapters.MetaTypeProviderAdapter
import interfaces._
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scala.Some

/**
 * Tests the adapters.
 */
class AdapterSpec extends WordSpecLike with Matchers {

  "Scala MetaTypeProvider" should {
    "be adaptable to an OSGi-compliant MetaTypeProvider" in {
      val adapter = new MetaTypeProviderAdapter(metaTypeProvider)

      // TODO Do some assertions
    }
  }

  /**
   * The test meta type provider.
   */
  lazy val metaTypeProvider = new MetaTypeProvider {
    def locales = List("EN")

    def getObjectClassDefinition(id: String, locale: Option[String]) = englishObjectClassDefinition
  }

  /**
   * The test object class definition in English language.
   */
  lazy val englishObjectClassDefinition = new ObjectClassDefinition {
    def id = "efed.de"
    def name = "wdfe"
    def description = ""
    def getIcon(size: Int) = None

    def requiredAttributeDefinitions = List(
      allowedFruitsAttributeDefinition,
      sizeAttributeDefinition
    )

    def optionalAttributeDefinitions = Nil
  }

  /**
   * The test attribute definition "Allowed Fruits".
   */
  lazy val allowedFruitsAttributeDefinition = new ListAttributeDefinition[String] {
    def id = "allowedFruits"
    def name = "Allowed fruits"
    def description = name
    def sizeLimit = None
    def defaultValue = Some(List("apples"))
    val valueType = classOf[String]

    def options = List(
      "Apples" -> "apples",
      "Bananas" -> "bananas"
    )

    def validate(value: String) = ValidationResult.NotValidated
  }

  /**
   * The test attribute definition "Size".
   */
  lazy val sizeAttributeDefinition = new ElementaryAttributeDefinition[Int] {
    import ValidationResult._

    def id = "size"
    def name = "Size"
    def description = name
    def defaultValue = None
    def options = None
    val valueType = classOf[Int]

    def validate(value: Int) = if (value < 0) Invalid("Value has to be positive") else Valid
  }
}
