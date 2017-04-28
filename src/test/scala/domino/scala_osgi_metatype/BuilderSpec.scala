package domino.scala_osgi_metatype

import builders.{ObjectClass, ListAttribute, ElementaryAttribute}
import interfaces._
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scala.Some

/**
 * Tests the builders.
 */
class BuilderSpec extends WordSpecLike with Matchers {

  "ObjectClass builder" should {
    "construct a corresponding ObjectClassDefinition" in {
      // TODO Do some assertions on the constructed object class definition
    }

    "construct an ObjectClassDefinition which can output a default configuration" in {
      val expectedMap = Map(
        "allowedFruits" -> List("apples"),
        "password" -> Password("secret01")
      )
      assert(objectClassDefinition.defaultConfig === expectedMap)
    }
  }

  /**
   * Creates the test object class definition.
   */
  lazy val objectClassDefinition = ObjectClass(
    id = "efed.de",
    requiredAttributes = List(
      ListAttribute[String](
        id = "allowedFruits",
        default = List("apples"),
        options = List("Apples" -> "apples", "Bananas" -> "bananas")
      ),
      ElementaryAttribute[Int](id = "size"),
      ElementaryAttribute[Password](id = "password", default = Some(Password("secret01")))
    )
  )
}
