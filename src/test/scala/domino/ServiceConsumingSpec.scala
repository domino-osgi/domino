package domino

import org.scalatest.WordSpecLike
import org.scalatest.ShouldMatchers
import org.osgi.framework.ServiceReference
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry
import domino.test.PojoSrTestHelper
import org.osgi.framework.InvalidSyntaxException
import java.util.Hashtable
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

object ServiceConsumingSpec {
  trait MyService {
    def doIt(): Int
  }
}

/**
 * Currently, tests that contain a `pending` tests only the DSL grammar and signatures but doesn't execute it.
 */
class ServiceConsumingSpec
    extends WordSpecLike
    with ShouldMatchers
    with PojoSrTestHelper {
  
  import ServiceConsumingSpec._

  "Reference: service consuming with PojoSR" should {

    "consume optional service restricted by filter" in {
      withPojoServiceRegistry { sr =>
        val props = new Hashtable[String, AnyRef](1)
        props.put("myProp", "myValue")
        sr.registerService(classOf[MyService].getName, new MyService { override def doIt() = 1 }, props)
        withStartedBundle(sr) {
          new BundleActivator {
            override def start(bundleContext: BundleContext): Unit = {
              // ensure the service is there
              assert(bundleContext.getServiceReference(classOf[MyService].getName) !== null)
              // now try with filter
              val services = bundleContext.getServiceReferences(classOf[MyService].getName, "(myProp=myValue)")
              assert(services.length === 1)
            }
            override def stop(bundleContext: BundleContext): Unit = {}
          }
        }
      }
    }

    "handle missing optional service restricted by filter" in {
      withPojoServiceRegistry { sr =>
        // we provide same service but with other properties
        sr.registerService(classOf[MyService].getName, new MyService { override def doIt() = 1 }, null)
        withStartedBundle(sr) {
          new BundleActivator {
            override def start(bundleContext: BundleContext): Unit = {
              // ensure the service is there
              assert(bundleContext.getServiceReference(classOf[MyService].getName) !== null)
              // no try with filter
              val services = bundleContext.getServiceReferences(classOf[MyService].getName, "(myProp=myValue)")
              assert(services === null)
            }
            override def stop(bundleContext: BundleContext): Unit = {}
          }
        }
      }
    }

  }

  "Service consuming" should {

    "handle missing optional service" in {
      withStartedBundle {
        new DominoActivator {
          whenBundleActive {
            val myService: Option[MyService] = service[MyService]
            assert(myService === None)
          }
        }
      }
    }

    "consume optional service" in {
      withPojoServiceRegistry { sr =>
        sr.registerService(classOf[MyService].getName, new MyService { override def doIt() = 1 }, null)
        withStartedBundle(sr) {
          new DominoActivator {
            whenBundleActive {
              val myService: Option[MyService] = service[MyService]
              assert(myService.get.doIt() === 1)
            }
          }
        }
      }
    }

    "consume optional service restricted by filter" in {
      withPojoServiceRegistry { sr =>
        val props = new Hashtable[String, AnyRef](1)
        props.put("myProp", "myValue")
        sr.registerService(classOf[MyService].getName, new MyService { override def doIt() = 1 }, props)
        withStartedBundle(sr) {
          new DominoActivator {
            whenBundleActive {
              // find the service without filter
              assert(service[MyService].get.doIt() === 1)
              // find the service with filter
              val myService: Option[MyService] = service[MyService]("(myProp=myValue)")
              assert(myService !== None)
              assert(myService.get.doIt() === 1)
            }
          }
        }
      }
    }

    "handle missing optional service restricted by filter" in {
      withPojoServiceRegistry { sr =>
        // we provide same service but with other properties
        sr.registerService(classOf[MyService].getName, new MyService { override def doIt() = 1 }, null)
        withStartedBundle(sr) {
          new DominoActivator {
            whenBundleActive {
              val myService: Option[MyService] = service[MyService](filter = "(myProp=myValue)")
              assert(myService === None)
            }
          }
        }
      }
    }

    "fail to offer optional service restricted by invalid filter" in {
      intercept[InvalidSyntaxException] {
        withStartedBundle {
          new DominoActivator {
            whenBundleActive {
              val myService: Option[MyService] = service[MyService](filter = "myProp=myValue")
            }
          }
        }
      }
    }

    "consume optional scoped service" in {
      new DominoActivator {
        whenBundleActive {
          val result: Int = withService[MyService, Int] {
            case Some(myService) => myService.doIt()
            case None => 5
          }
        }
      }
      pending
    }

    "consume implicit withService on reference" in {
      new DominoActivator {
        whenBundleActive {
          serviceRef[MyService] foreach { r: ServiceReference[MyService] =>
            r.withService { s: Option[MyService] =>

            }
          }
        }
      }
      pending
    }

    "consume optional scoped service restricted by filter" in {
      new DominoActivator {
        whenBundleActive {
          val result: Int = withAdvancedService[MyService, Int]("(myProp=myValue)") {
            case Some(myService) => myService.doIt()
            case None => 5
          }
        }
      }
      pending
    }

    "consume optional service reference" in {
      new DominoActivator {
        whenBundleActive {
          serviceRef[MyService] foreach { r: ServiceReference[MyService] =>

          }
        }
      }
      pending
    }

    "consume implicit service on reference" in {
      new DominoActivator {
        whenBundleActive {
          serviceRef[MyService] foreach { r: ServiceReference[MyService] =>
            val myService: Option[MyService] = r.service
          }
        }
      }
      pending
    }

    "consume optional service reference restricted by filter" in {
      new DominoActivator {
        whenBundleActive {
          val ref: Option[ServiceReference[MyService]] = serviceRef[MyService]("(myProp=myValue)")
        }
      }
      pending
    }

    "consume multiple services" in {
      new DominoActivator {
        whenBundleActive {
          val s: Traversable[MyService] = services[MyService]
        }
      }
      pending
    }

    "consume multiple services restricted by filter" in {
      new DominoActivator {
        whenBundleActive {
          val s: Traversable[MyService] = services[MyService]("(myProp=MyValue)")
        }
      }
      pending
    }

    "consume multiple service references" in {
      new DominoActivator {
        whenBundleActive {
          val refs: Traversable[ServiceReference[MyService]] = serviceRefs[MyService]
        }
      }
      pending
    }

    "consume multiple service references restricted by filter" in {
      new DominoActivator {
        whenBundleActive {
          val refs: Traversable[ServiceReference[MyService]] = serviceRefs[MyService]("(myProp=MyValue)")
        }
      }
      pending
    }
  }

}