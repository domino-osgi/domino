package org.helgoboss.dominoe

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.framework.ServiceRegistration

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
@RunWith(classOf[JUnitRunner])
class ServiceProvidingSpec extends DominoeActivator with WordSpec with ShouldMatchers {

  trait MyService {
    def doIt()
  }

  trait MyService2

  class CombinedService extends MyService with MyService2 {
    def doIt() {}
  }

  val exampleService = new MyService with MyService2 {
    def doIt() {}
  }

  val combinedService = new CombinedService



  val serviceProps = Map("prop1" -> "value1", "prop2" -> 3)

  "Service providing" should {

    "allow specifying just one interface" in {
      whenBundleActive {
        val reg: ServiceRegistration[CombinedService] = combinedService.providesService[CombinedService]
      }
    }

    "allow specifying just one interface and passing service properties" in {
      whenBundleActive {
        val reg: ServiceRegistration[_] = exampleService.providesService[MyService](
          "prop1" -> "value1",
          "prop2" -> 3
        )
      }
    }

    "allow specifying just one interface and passing service properties in a map" in {
      whenBundleActive {
        val reg: ServiceRegistration[_] = exampleService.providesService[MyService](serviceProps)
      }
    }

    "allow specifying several interfaces" in {
      whenBundleActive {
        val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2]
      }
    }

    "allow specifying several interfaces and passing service properties" in {
      whenBundleActive {
        val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2](
          "prop1" -> "value1",
          "prop2" -> 3
        )
      }
    }

    "allow specifying several interfaces and passing service properties in a map" in {
      whenBundleActive {
        val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2](serviceProps)
      }
    }

    "allow specifying generic types" in {
      whenBundleActive {
        val reg: ServiceRegistration[_] = List(exampleService).providesService[List[MyService]]
      }
    }
  }
}