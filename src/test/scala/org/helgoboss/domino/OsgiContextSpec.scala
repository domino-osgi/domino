package org.helgoboss.domino

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest._

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
@RunWith(classOf[JUnitRunner])
class OsgiContextSpec extends DominoActivator with WordSpecLike with ShouldMatchers {

  trait Service1

  trait Service2

  trait Service3

  "OsgiContext" should {
    "bind a capsule scope to the bundle life cycle" in {
      whenBundleActive {
        onStart {
          println("start")
        }
        onStop {
          println("end")
        }
      }
    }
  }

}