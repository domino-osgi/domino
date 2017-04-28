package domino

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.osgi.util.tracker.ServiceTracker
import service_watching.ServiceWatcherContext
import service_watching.ServiceWatcherEvent.AddingService
import service_watching.ServiceWatcherEvent.ModifiedService
import service_watching.ServiceWatcherEvent.RemovedService
import org.osgi.framework.InvalidSyntaxException
import org.osgi.framework.Filter
import org.osgi.framework.FrameworkUtil
import domino.test.PojoSrTestHelper

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class ServiceWatchingSpec
    extends WordSpecLike
    with Matchers
    with PojoSrTestHelper {

  trait MyService {
    def doIt()
  }

  trait MyOtherService {
  }

  "Reference: Plain OSGi" should {
    "parse a correct LDAP filter string" in {
      val filter = FrameworkUtil.createFilter("(objectClass=java.io.File)")
      assert(filter.isInstanceOf[Filter])

    }
    "not parse an invalid LDAP filter string" in {
      intercept[InvalidSyntaxException] { FrameworkUtil.createFilter("objectClass=java.io.File") }
    }
  }

  "Service watching" should {

    "enable waiting until a particular service becomes available" in {
      new DominoActivator {
        whenBundleActive {
          val tracker: ServiceTracker[MyService, MyService] = whenServicePresent[MyService] { myService: MyService =>
          }
        }
      }
      pending
    }

    "enable waiting until a particular service restricted with a filter expression becomes available" in {
      new DominoActivator {
        whenBundleActive {
          val tracker: ServiceTracker[MyService, MyService] = whenAdvancedServicePresent[MyService]("(transactional=true)") { myService: MyService =>
          }
        }
      }
      pending
    }

    "enable waiting until several particular services are available implicitly" in {
      new DominoActivator {
        whenBundleActive {
          val myServiceTracker: ServiceTracker[MyService, MyService] = whenServicePresent[MyService] { myService: MyService =>
            val myOtherServiceTracker: ServiceTracker[MyOtherService, MyOtherService] = whenServicePresent[MyOtherService] { myOtherService: MyOtherService =>
            }
          }
        }
      }
      pending
    }

    "enable waiting until several particular services are available explicitly" in {
      new DominoActivator {
        whenBundleActive {
          val myServiceTracker: ServiceTracker[MyService, MyService] = whenServicesPresent[MyService, MyOtherService] { (myService: MyService, myOtherService: MyOtherService) =>
          }
        }
      }
      pending
    }

    "let you react to every possible event" in {
      new DominoActivator {
        whenBundleActive {
          val tracker: ServiceTracker[MyService, MyService] = watchServices[MyService] {
            case AddingService(s: MyService, c: ServiceWatcherContext[MyService]) =>
            case RemovedService(s: MyService, c: ServiceWatcherContext[MyService]) =>
            case ModifiedService(s: MyService, c: ServiceWatcherContext[MyService]) =>
          }
        }
      }
      pending
    }

    "let you react to every possible event with filters" in {
      new DominoActivator {
        whenBundleActive {
          val tracker: ServiceTracker[MyService, MyService] = watchAdvancedServices[MyService]("(transactional=true)") {
            case AddingService(s: MyService, c: ServiceWatcherContext[MyService]) =>
            case RemovedService(s: MyService, c: ServiceWatcherContext[MyService]) =>
            case ModifiedService(s: MyService, c: ServiceWatcherContext[MyService]) =>
          }
        }
      }
      pending
    }

    "break with an exception with invalid filters" in {
      intercept[InvalidSyntaxException] {
        withStartedBundle {
          new DominoActivator {
            whenBundleActive {
              val tracker: ServiceTracker[MyService, MyService] = watchAdvancedServices[MyService]("transactional=true") {
                case _ =>
              }
            }
          }
        }
      }
    }

  }

}
