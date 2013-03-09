package org.helgoboss.dominoe

import bundle_watching.AddingBundle
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import service_watching.AddingService

@RunWith(classOf[JUnitRunner])
class OsgiContextSpec extends WordSpec with ShouldMatchers {
    
    trait Service1
    
    trait Service2
    
    trait Service3
        
    object MyBundleActivator extends DominoeActivator with Service1 {
        whenBundleActive {
            this.providesService[Service1]
            onStart {
                println("start")
            }
            onStop {
                println("end")
            }
            whenServicePresent[Service2] { service1 =>
                watchServices[Service3] {
                    case AddingService(s, c) =>
                    case _ =>
                } 
                watchBundles { event =>
                    val result = event match {
                        case AddingBundle(s, c) => "test"
                        case _ => "test2"
                    }
                    println(result)
                }
            } 
        }
    }
    
}