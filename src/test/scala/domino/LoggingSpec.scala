package domino

import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import domino.logging.Logging

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class LoggingSpec extends DominoActivator with Logging with WordSpecLike with ShouldMatchers {

  "Logging" should {

    "provide logging" in {
      whenBundleActive {
        log.debug("Hello!")
      }
      pending
    }

  }
}