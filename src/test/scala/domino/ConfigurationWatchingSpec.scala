package domino

import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import org.osgi.framework.ServiceRegistration
import domino.scala_osgi_metatype.builders.ObjectClass
import org.osgi.service.cm.{ ManagedServiceFactory, ManagedService }

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class ConfigurationWatchingSpec
    extends WordSpecLike
    with ShouldMatchers {

  val objectClass = ObjectClass(id = "domino.test", name = "Test")

  "Configuration watching" should {

    "work with normal configurations" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[ManagedService] = whenConfigurationActive("domino.test") { conf: Map[String, Any] =>
          }
        }
      }
      pending
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