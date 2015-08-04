package domino

import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import org.osgi.framework.ServiceRegistration
import domino.test.PojoSrTestHelper

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class ServiceProvidingSpec
    extends WordSpecLike
    with ShouldMatchers {

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
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[CombinedService] = combinedService.providesService[CombinedService]
        }
      }
      pending
    }

    "allow specifying just one interface and passing service properties" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService](
            "prop1" -> "value1",
            "prop2" -> 3
          )
        }
      }
      pending
    }

    "allow specifying just one interface and passing service properties in a map" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService](serviceProps)
        }
      }
      pending
    }

    "allow specifying several interfaces" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2]
        }
      }
      pending
    }

    "allow specifying several interfaces and passing service properties" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2](
            "prop1" -> "value1",
            "prop2" -> 3
          )
        }
      }
      pending
    }

    "allow specifying several interfaces and passing service properties in a map" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2](serviceProps)
        }
      }
      pending
    }

    "allow specifying generic types" in {
      new DominoActivator {
        whenBundleActive {
          val reg: ServiceRegistration[_] = List(exampleService).providesService[List[MyService]]
        }
      }
      pending
    }
  }
}