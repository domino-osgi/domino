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
import org.osgi.framework.BundleActivator

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

    def testConfigurations(f: Log => BundleActivator): Unit = {
      withPojoServiceRegistry { sr =>
        val log = new Log
        def bundleContext: BundleContext = sr.getBundleContext

        val activator = f(log)
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

        assert(log.log === List(
          "config: List()",
          "stop config: List()",
          "config: List(prop1=v1, service.pid=domino.test)"))
      }
    }

    "work with normal configurations" in {
      testConfigurations(log => new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[ManagedService] = whenConfigurationActive("domino.test") { conf: Map[String, Any] =>
            log.log("config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
            onStop {
              log.log("stop config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
            }
          }
        }
      })
    }

    "work with normal configurations when CM is already running" in {
      withPojoServiceRegistry { sr =>
        val log = new Log
        def bundleContext: BundleContext = sr.getBundleContext

        val cm = new ConfigurationManager()
        cm.start(bundleContext)

        val ref = bundleContext.getServiceReference(classOf[ConfigurationAdmin])
        val ca: ConfigurationAdmin = bundleContext.getService(ref)

        val config = ca.getConfiguration("domino.test2")
        config.update(mutable.Map("prop1" -> "v1").asJavaDictionary)

        // make sure no outstanding events exists
        Thread.sleep(500)

        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[ManagedService] = whenConfigurationActive("domino.test2") { conf: Map[String, Any] =>
              log.log("config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
              onStop {
                log.log("stop config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
              }
            }
          }
        }
        activator.start(bundleContext)

        // make sure no outstanding events exists
        Thread.sleep(500)

        assert(log.log === List("config: List(prop1=v1, service.pid=domino.test2)"))
      }
    }

    "work with normal configurations and metatypes" in {
      testConfigurations(log => new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[ManagedService] = whenConfigurationActive(objectClass) { conf: Map[String, Any] =>
            log.log("config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
            onStop {
              log.log("stop config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
            }
          }
        }
      })
    }

    def testFactoryConfigurations(f: Log => BundleActivator): Unit = {
      withPojoServiceRegistry { sr =>
        def bundleContext = sr.getBundleContext()
        val log = new Log

        val cm = new ConfigurationManager()
        cm.start(bundleContext)

        val ref = bundleContext.getServiceReference(classOf[ConfigurationAdmin])
        val ca: ConfigurationAdmin = bundleContext.getService(ref)

        val activator = f(log)
        activator.start(bundleContext)
        assert(log.log === List())

        val config1 = ca.createFactoryConfiguration("domino.test")
        config1.update(mutable.Map("prop1" -> "1").asJavaDictionary)
        Thread.sleep(500)
        assert(log.log.size === 1, "- Log has wrong size: " + log.log)
        log.log.zip(List(
          "config: List(prop1=1, service.factoryPid=domino.test, service.pid=domino.test."
        )).foreach {
          case (msg, expected) =>
            msg should startWith(expected)
        }

        val config2 = ca.createFactoryConfiguration("domino.test")
        config2.update(mutable.Map("prop2" -> "2").asJavaDictionary)
        Thread.sleep(500)
        assert(log.log.size === 2, "- Log has wrong size: " + log.log)
        log.log.zip(List(
          "config: List(prop1=1, service.factoryPid=domino.test, service.pid=domino.test.",
          "config: List(prop2=2, service.factoryPid=domino.test, service.pid=domino.test."
        )).foreach {
          case (msg, expected) =>
            msg should startWith(expected)
        }

        // resend config to test double-updates
        config2.update(mutable.Map("prop2" -> "2").asJavaDictionary)
        // expect no new config update
        Thread.sleep(500)
        assert(log.log.size === 2, "- Log has wrong size: " + log.log)
        log.log.zip(List(
          "config: List(prop1=1, service.factoryPid=domino.test, service.pid=domino.test.",
          "config: List(prop2=2, service.factoryPid=domino.test, service.pid=domino.test."
        )).foreach {
          case (msg, expected) =>
            msg should startWith(expected)
        }

        // delete on config
        config1.delete()
        // expect no new config update
        Thread.sleep(500)
        assert(log.log.size === 3, "- Log has wrong size: " + log.log)
        log.log.zip(List(
          "config: List(prop1=1, service.factoryPid=domino.test, service.pid=domino.test.",
          "config: List(prop2=2, service.factoryPid=domino.test, service.pid=domino.test.",
          "stop config: List(prop1=1, service.factoryPid=domino.test, service.pid=domino.test."
        )).foreach {
          case (msg, expected) =>
            msg should startWith(expected)
        }
      }
    }

    "work with factory configurations" in {
      testFactoryConfigurations(log => new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[ManagedServiceFactory] = whenFactoryConfigurationActive("domino.test", "Test") {
            (conf: Map[String, Any], pid: String) =>
              log.log("config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
              onStop {
                log.log("stop config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
              }
          }
        }
      })
    }

    "work with factory configurations and metatypes" in {
      testFactoryConfigurations(log => new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[ManagedServiceFactory] = whenFactoryConfigurationActive(objectClass) {
            (conf: Map[String, Any], pid: String) =>
              log.log("config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
              onStop {
                log.log("stop config: " + conf.map(c => c._1 + "=" + c._2).toList.sorted)
              }
          }
        }
      })
    }

  }

}