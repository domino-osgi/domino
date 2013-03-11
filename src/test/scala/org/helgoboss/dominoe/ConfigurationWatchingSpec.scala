package org.helgoboss.dominoe

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.framework.{ServiceRegistration, ServiceReference, BundleContext}
import org.helgoboss.scala_osgi_metatype.builders.ObjectClass
import org.osgi.service.cm.{ManagedServiceFactory, ManagedService}

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
@RunWith(classOf[JUnitRunner])
class ConfigurationWatchingSpec extends DominoeActivator with WordSpec with ShouldMatchers {
  val objectClass = ObjectClass(id = "org.helgoboss.test", name = "Test")

  "Configuration watching" should {

    "work with normal configurations" in {
      whenBundleActive {
        val reg: ServiceRegistration[ManagedService] = whenConfigurationActive("org.helgoboss.test") { conf: Map[String, Any] =>
        }
      }
    }

    "work with normal configurations and metatypes" in {
      whenBundleActive {
        val reg: ServiceRegistration[ManagedService] = whenConfigurationActive(objectClass) { conf: Map[String, Any] =>
        }
      }
    }

    "work with factory configurations" in {
      whenBundleActive {
        val reg: ServiceRegistration[ManagedServiceFactory] = whenFactoryConfigurationActive("org.helgoboss.test", "Test") {
          (conf: Map[String, Any], pid: String) =>
        }
      }
    }

    "work with factory configurations and metatypes" in {
      whenBundleActive {
        val reg: ServiceRegistration[ManagedServiceFactory] = whenFactoryConfigurationActive(objectClass) {
          (conf: Map[String, Any], pid: String) =>
        }
      }
    }

  }

}