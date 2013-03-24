package org.helgoboss.domino

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.framework.ServiceReference

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
@RunWith(classOf[JUnitRunner])
class LoggingSpec extends DominoActivator with WordSpec with ShouldMatchers {

  "Logging" should {

    "provide logging" in {
      whenBundleActive {
        log.debug("Hello!")
      }
    }

  }
}