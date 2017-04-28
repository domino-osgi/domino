package domino

import org.scalatest.WordSpecLike
import org.scalatest.Matchers

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class OsgiContextSpec
    extends WordSpecLike
    with Matchers {

  trait Service1

  trait Service2

  trait Service3

  "OsgiContext" should {
    "bind a capsule scope to the bundle life cycle" in {
      new DominoActivator {
        whenBundleActive {
          onStart {
            println("start")
          }
          onStop {
            println("end")
          }
        }
      }
      pending
    }
  }

}
