package org.helgoboss.dominoe

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.framework.ServiceRegistration

@RunWith(classOf[JUnitRunner])
class ServiceProviderSpec extends DominoeActivator with WordSpec with ShouldMatchers {
    trait MyService {
        def doIt()
    }
    
    trait MyService2 {
    }
    
    val exampleService = new MyService with MyService2 {
        def doIt() {}
    }
    
    "An OsgiProvider" should {
        "be able to provide services just by specifying an interface" in {
            whenBundleActive {
                val reg: ServiceRegistration = exampleService.providesService[MyService]
            }
        }
        
        "be able to provide services with properties set" in {
            whenBundleActive {
                exampleService.providesService[MyService]("prop1" -> "value1", "prop2" -> 3)
            }
        }
        
        "be able to provide services under several interfaces with properties set" in {
            whenBundleActive {
                exampleService.providesService[MyService, MyService2]("prop1" -> "value1", "prop2" -> 3)
            }
        }
        
        "be able to provide services with properties set in a map" in {
            whenBundleActive {
                exampleService.providesService[MyService](Map("prop1" -> "value1", "prop2" -> 3))
            }
        }
    }
}