package org.helgoboss.domino

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import org.osgi.util.tracker.ServiceTracker
import service_watching.ServiceWatcherContext
import service_watching.ServiceWatcherEvent._

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
@RunWith(classOf[JUnitRunner])
class ServiceWatchingSpec extends DominoActivator with WordSpecLike with ShouldMatchers {

  trait MyService {
    def doIt()
  }

  trait MyOtherService {
  }

  "Service watching" should {

    "enable waiting until a particular service becomes available" in {
      whenBundleActive {
        val tracker: ServiceTracker[MyService, MyService] = whenServicePresent[MyService] { myService: MyService =>
        }
      }
    }

    "enable waiting until a particular service restricted with a filter expression becomes available" in {
      whenBundleActive {
        val tracker: ServiceTracker[MyService, MyService] = whenAdvancedServicePresent[MyService]("(transactional=true)") { myService: MyService =>
        }
      }
    }

    "enable waiting until several particular services are available implicitly" in {
      whenBundleActive {
        val myServiceTracker: ServiceTracker[MyService, MyService] = whenServicePresent[MyService] { myService: MyService =>
          val myOtherServiceTracker: ServiceTracker[MyOtherService, MyOtherService] = whenServicePresent[MyOtherService] { myOtherService: MyOtherService =>
          }
        }
      }
    }

    "enable waiting until several particular services are available explicitly" in {
      whenBundleActive {
        val myServiceTracker: ServiceTracker[MyService, MyService] = whenServicesPresent[MyService, MyOtherService] { (myService: MyService, myOtherService: MyOtherService) =>
        }
      }
    }

    "let you react to every possible event" in {
      whenBundleActive {
        val tracker: ServiceTracker[MyService, MyService] = watchServices[MyService] {
          case AddingService(s: MyService, c: ServiceWatcherContext[MyService]) =>
          case RemovedService(s: MyService, c: ServiceWatcherContext[MyService]) =>
          case ModifiedService(s: MyService, c: ServiceWatcherContext[MyService]) =>
        }
      }
    }

    "let you react to every possible event with filters" in {
      whenBundleActive {
        val tracker: ServiceTracker[MyService, MyService] = watchAdvancedServices[MyService]("(transactional=true)") {
          case AddingService(s: MyService, c: ServiceWatcherContext[MyService]) =>
          case RemovedService(s: MyService, c: ServiceWatcherContext[MyService]) =>
          case ModifiedService(s: MyService, c: ServiceWatcherContext[MyService]) =>
        }
      }
    }
  }

}