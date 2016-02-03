package domino

import org.osgi.framework.ServiceRegistration
import org.osgi.service.cm.ManagedService
import org.osgi.service.cm.ManagedServiceFactory
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import domino.scala_osgi_metatype.builders.ObjectClass
import domino.test.PojoSrTestHelper

object ConfigurationWatchingSpec {
  class Log {
    private[this] var journal = List[String]()
    def log(msg: String): Unit = {
      journal = msg :: journal
    }
    def log: List[String] = journal.reverse
  }
}

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class ConfigurationWatchingSpec
    extends WordSpecLike
    with Matchers
    with PojoSrTestHelper {

  import ConfigurationWatchingSpec._

  val objectClass = ObjectClass(id = "domino.test", name = "Test")

  "Configuration watching" should {

    "work with normal configurations" in {
      val log = new Log
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[ManagedService] = whenConfigurationActive("domino.test") { conf: Map[String, Any] =>
              log.log("config: " + conf)
            }
          }
        }
        activator.start(sr.getBundleContext)
        // empty config, as we don't have a ConfigAdminService
        assert(log.log === List("config: Map()"))
      }
    }

    "work with normal configurations and metatypes" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[ManagedService] = whenConfigurationActive(objectClass) { conf: Map[String, Any] =>
          }
        }
      }
      pending
    }

    "work with factory configurations" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[ManagedServiceFactory] = whenFactoryConfigurationActive("domino.test", "Test") {
            (conf: Map[String, Any], pid: String) =>
          }
        }
      }
      pending
    }

    "work with factory configurations and metatypes" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[ManagedServiceFactory] = whenFactoryConfigurationActive(objectClass) {
            (conf: Map[String, Any], pid: String) =>
          }
        }
      }
      pending
    }

  }

}