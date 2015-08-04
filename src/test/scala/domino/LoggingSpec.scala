package domino

import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import domino.logging.Logging
import domino.test.PojoSrTestHelper
import org.osgi.service.log.LogService
import org.osgi.service.log.LogReaderService
import scala.collection.JavaConverters._

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class LoggingSpec
    extends WordSpecLike
    with ShouldMatchers
    with PojoSrTestHelper {

  "Logging" should {

    "provide logging when no LogService is present" in {
      withPojoServiceRegistry { sr =>
        assert(sr.getServiceReference(classOf[LogService].getName) === null)
        withStartedBundle(sr) {
          new DominoActivator with Logging {
            whenBundleActive {
              log.debug("Hello!")
            }
          }
        }
      }
    }

  }
}