package org.helgoboss.domino

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
@RunWith(classOf[JUnitRunner])
class LoggingSpec extends DominoActivator with WordSpecLike with ShouldMatchers {

  "Logging" should {

    "provide logging" in {
      whenBundleActive {
        log.debug("Hello!")
      }
    }

  }
}