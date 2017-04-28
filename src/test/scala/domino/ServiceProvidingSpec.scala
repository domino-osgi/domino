package domino

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.osgi.framework.ServiceRegistration
import domino.test.PojoSrTestHelper
import domino.test.PojoSrTestHelper
import org.osgi.framework.ServiceListener

object ServiceProvidingSpec {

  trait MyService {
    def doIt()
  }

  trait MyService2

  class CombinedService extends MyService with MyService2 {
    def doIt() {}
  }

}

/**
 * Currently tests only the DSL grammar and signatures but doesn't execute it.
 */
class ServiceProvidingSpec
    extends WordSpecLike
    with Matchers
    with PojoSrTestHelper {

  import ServiceProvidingSpec._

  val exampleService = new MyService with MyService2 {
    def doIt() {}
  }

  val combinedService = new CombinedService

  val serviceProps = Map("prop1" -> "value1", "prop2" -> 3)

  "Service providing" should {

    "allow specifying just one interface" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            combinedService.providesService[CombinedService]
          }
        }
        activator.start(sr.getBundleContext)
        val ref = sr.getServiceReference(classOf[CombinedService].getName)
        assert(ref !== null)
        assert(sr.getService(ref).isInstanceOf[CombinedService])
        assert(sr.getServiceReference(classOf[MyService].getName) === null)
        assert(sr.getServiceReference(classOf[MyService2].getName) === null)
      }
    }

    "allow specifying just one interface and passing service properties" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[_] = exampleService.providesService[MyService](
              "prop1" -> "value1",
              "prop2" -> 3
            )
          }
        }
        activator.start(sr.getBundleContext)
        val ref = sr.getServiceReference(classOf[MyService].getName)
        assert(ref !== null)
        assert(sr.getService(ref).isInstanceOf[MyService])
        assert(ref.getProperty("prop1") === "value1")
        assert(ref.getProperty("prop2") === 3)
        assert(sr.getServiceReference(classOf[MyService2].getName) === null)
        assert(sr.getServiceReferences(classOf[MyService].getName, "(&(prop1=value1)(prop2=3))") !== null)
      }
    }

    "allow specifying just one interface and passing service properties in a map" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[_] = exampleService.providesService[MyService](serviceProps)
          }
        }
        activator.start(sr.getBundleContext)
        val ref = sr.getServiceReference(classOf[MyService].getName)
        assert(ref !== null)
        assert(sr.getService(ref).isInstanceOf[MyService])
        assert(ref.getProperty("prop1") === "value1")
        assert(ref.getProperty("prop2") === 3)
        assert(sr.getServiceReference(classOf[MyService2].getName) === null)
        assert(sr.getServiceReferences(classOf[MyService].getName, "(&(prop1=value1)(prop2=3))") !== null)
      }
    }

    "allow specifying several interfaces" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2]
          }
        }
        activator.start(sr.getBundleContext)
        val ref1 = sr.getServiceReference(classOf[MyService].getName)
        assert(ref1 !== null)
        val ref2 = sr.getServiceReference(classOf[MyService2].getName)
        assert(ref2 !== null)
        assert(sr.getService(ref1) === sr.getService(ref2))
      }
    }

    "allow specifying several interfaces and passing service properties" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2](
              "prop1" -> "value1",
              "prop2" -> 3
            )
          }
        }
        activator.start(sr.getBundleContext)
        val ref1 = sr.getServiceReference(classOf[MyService].getName)
        assert(ref1 !== null)
        assert(sr.getService(ref1).isInstanceOf[MyService])
        assert(ref1.getProperty("prop1") === "value1")
        assert(ref1.getProperty("prop2") === 3)
        val ref2 = sr.getServiceReference(classOf[MyService2].getName)
        assert(ref1 !== null)
        assert(sr.getService(ref1).isInstanceOf[MyService2])
        assert(ref1.getProperty("prop1") === "value1")
        assert(ref1.getProperty("prop2") === 3)
        assert(sr.getServiceReferences(classOf[MyService].getName, "(&(prop1=value1)(prop2=3))") !== null)
        assert(sr.getServiceReferences(classOf[MyService2].getName, "(&(prop1=value1)(prop2=3))") !== null)
        assert(sr.getService(ref1) === sr.getService(ref2))
      }
    }

    "allow specifying several interfaces and passing service properties in a map" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[_] = exampleService.providesService[MyService, MyService2](serviceProps)
          }
        }
        activator.start(sr.getBundleContext)
        val ref1 = sr.getServiceReference(classOf[MyService].getName)
        assert(ref1 !== null)
        assert(sr.getService(ref1).isInstanceOf[MyService])
        assert(ref1.getProperty("prop1") === "value1")
        assert(ref1.getProperty("prop2") === 3)
        val ref2 = sr.getServiceReference(classOf[MyService2].getName)
        assert(ref1 !== null)
        assert(sr.getService(ref1).isInstanceOf[MyService2])
        assert(ref1.getProperty("prop1") === "value1")
        assert(ref1.getProperty("prop2") === 3)
        assert(sr.getServiceReferences(classOf[MyService].getName, "(&(prop1=value1)(prop2=3))") !== null)
        assert(sr.getServiceReferences(classOf[MyService2].getName, "(&(prop1=value1)(prop2=3))") !== null)
        assert(sr.getService(ref1) === sr.getService(ref2))
      }
    }

    "allow specifying generic types" in {
      withPojoServiceRegistry { sr =>
        val activator = new DominoActivator {
          whenBundleActive {
            val reg: ServiceRegistration[_] = List(exampleService).providesService[List[MyService]]
          }
        }
        activator.start(sr.getBundleContext)

        val genericValue = ";List[domino.ServiceProvidingSpec.MyService];"

        val ref = sr.getServiceReference(classOf[List[MyService]].getName)
        assert(ref.getProperty(DominoUtil.GenericsExpressionKey) === genericValue)

        val refs = sr.getServiceReferences(classOf[List[MyService]].getName, s"(${DominoUtil.GenericsExpressionKey}=${genericValue})")
        assert(refs !== null)
      }
    }
  }
}
