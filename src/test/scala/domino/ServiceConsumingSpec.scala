package domino

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import org.osgi.framework.ServiceReference

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
@RunWith(classOf[JUnitRunner])
class ServiceConsumingSpec extends DominoActivator with WordSpecLike with ShouldMatchers {

  trait MyService {
    def doIt(): Int
  }

  "Service consuming" should {

    "offer optional service" in {
      whenBundleActive {
        val myService: Option[MyService] = service[MyService]
      }
    }

    "offer optional service restricted by filter" in {
      whenBundleActive {
        val myService: Option[MyService] = service[MyService](filter = "(myProp=myValue)")
      }
    }

    "offer optional scoped service" in {
      whenBundleActive {
        val result: Int = withService[MyService, Int] {
          case Some(myService) => myService.doIt()
          case None => 5
        }
      }
    }

    "offer implicit withService on reference" in {
      whenBundleActive {
        serviceRef[MyService] foreach { r: ServiceReference[MyService] =>
          r.withService { s: Option[MyService] =>

          }
        }
      }
    }

    "offer optional scoped service restricted by filter" in {
      whenBundleActive {
        val result: Int = withAdvancedService[MyService, Int]("(myProp=myValue)") {
          case Some(myService) => myService.doIt()
          case None => 5
        }
      }
    }

    "offer optional service reference" in {
      whenBundleActive {
        serviceRef[MyService] foreach { r: ServiceReference[MyService] =>

        }
      }
    }

    "offer implicit service on reference" in {
      whenBundleActive {
        serviceRef[MyService] foreach { r: ServiceReference[MyService] =>
          val myService: Option[MyService] = r.service
        }
      }
    }

    "offer optional service reference restricted by filter" in {
      whenBundleActive {
        val ref: Option[ServiceReference[MyService]] = serviceRef[MyService]("(myProp=myValue)")
      }
    }

    "offer multiple services" in {
      whenBundleActive {
        val s: Traversable[MyService] = services[MyService]
      }
    }

    "offer multiple services restricted by filter" in {
      whenBundleActive {
        val s: Traversable[MyService] = services[MyService]("(myProp=MyValue)")
      }
    }

    "offer multiple service references" in {
      whenBundleActive {
        val refs: Traversable[ServiceReference[MyService]] = serviceRefs[MyService]
      }
    }

    "offer multiple service references restricted by filter" in {
      whenBundleActive {
        val refs: Traversable[ServiceReference[MyService]] = serviceRefs[MyService]("(myProp=MyValue)")
      }
    }

  }
}