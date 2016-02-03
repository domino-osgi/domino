package domino

import org.osgi.framework.ServiceRegistration
import org.osgi.service.cm.ManagedService
import org.osgi.service.cm.ManagedServiceFactory
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import domino.test.PojoSrTestHelper

import domino.scala_osgi_metatype.builders.ObjectClass
import org.osgi.framework.BundleContext
import org.apache.felix.cm.impl.ConfigurationManager
import org.osgi.service.cm.ConfigurationAdmin
import scala.collection.mutable

import scala.collection.JavaConverters._

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
      withPojoServiceRegistry { sr =>
        val log = new Log
        def bundleContext: BundleContext = sr.getBundleContext

        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[ManagedService] = whenConfigurationActive("domino.test") { conf: Map[String, Any] =>
              log.log("config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
            }
          }
        }
        activator.start(bundleContext)
        // empty config, as we don't have a ConfigAdminService
        assert(log.log === List("config: List()"))

        val cm = new ConfigurationManager()
        cm.start(bundleContext)

        val ref = bundleContext.getServiceReference(classOf[ConfigurationAdmin])
        val ca: ConfigurationAdmin = bundleContext.getService(ref)

        val config = ca.getConfiguration("domino.test")
        config.update(mutable.Map("prop1" -> "v1").asJavaDictionary)

        // make sure no outstanding events exists
        Thread.sleep(500)

        assert(log.log === List("config: List()", "config: List(prop1=v1, service.pid=domino.test)"))
      }
    }

    "work with normal configurations when CM is already running" in {
      withPojoServiceRegistry { sr =>
        val log = new Log
        def bundleContext: BundleContext = sr.getBundleContext

        val cm = new ConfigurationManager()
        cm.start(bundleContext)

        val ref = bundleContext.getServiceReference(classOf[ConfigurationAdmin])
        val ca: ConfigurationAdmin = bundleContext.getService(ref)

        val config = ca.getConfiguration("domino.test")
        config.update(mutable.Map("prop1" -> "v1").asJavaDictionary)

        // make sure no outstanding events exists
        Thread.sleep(500)

        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[ManagedService] = whenConfigurationActive("domino.test") { conf: Map[String, Any] =>
              log.log("config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
            }
          }
        }
        activator.start(bundleContext)

        assert(log.log === List("config: List(prop1=v1, service.pid=domino.test)"))
      }
    }

    "work with normal configurations and metatypes" in {
      val log = new Log
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[ManagedService] = whenConfigurationActive(objectClass) { conf: Map[String, Any] =>
              log.log("config: " + conf)
            }
          }
        }
        activator.start(sr.getBundleContext)
        // empty config, as we don't have a ConfigAdminService
        assert(log.log === List("config: Map()"))
        // TODO: check with ConfigAdmin
        pending
      }
    }

    "work with factory configurations" in {
      val log = new Log
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[ManagedServiceFactory] = whenFactoryConfigurationActive("domino.test", "Test") {
              (conf: Map[String, Any], pid: String) =>
                log.log("pid: " + pid + " config: " + conf)
            }
          }
        }
        activator.start(sr.getBundleContext)
        assert(log.log === List())
        // TODO: check with ConfigAdmin
        pending
      }
    }

    "work with factory configurations and metatypes" in {
      val log = new Log
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[ManagedServiceFactory] = whenFactoryConfigurationActive(objectClass) {
              (conf: Map[String, Any], pid: String) =>
                log.log("pid: " + pid + " config: " + conf)
            }
          }
        }
        activator.start(sr.getBundleContext)
        assert(log.log === List())
        // TODO: check with ConfigAdmin
        pending
      }
    }

    "ignore config updates without change in configuration" in {
      pending
    }

  }

}