package org.helgoboss.dominoe

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.util.tracker.ServiceTracker

@RunWith(classOf[JUnitRunner])
class ServiceWatcherSpec extends DominoeActivator with WordSpec with ShouldMatchers {
    trait MyService {
        def doIt()
    }
    
    trait MyOtherService {
    }
    
    "An OsgiServiceWatcher" should {
        "be able to watch adding, removal and modification of services" in {
            whenBundleActive {
                val tracker: ServiceTracker = watchServices[MyService] {
                    case AddingService(s, c) =>                 
                    case RemovedService(s, c) =>
                    case ModifiedService(s, c) =>
                }
            }
        }
        "be able to execute code as soon as a service of a given type gets available" in {
            whenBundleActive {
                val tracker: ServiceTracker = whenServicePresent[MyService] { myService =>
                }
            }
        }
        "be able to execute code as soon as all services of the given types get available" in {
            whenBundleActive {
                whenServicesPresent[MyService, MyOtherService] { (myService, myOtherService) =>
                }
            }
        }
    }
    
}