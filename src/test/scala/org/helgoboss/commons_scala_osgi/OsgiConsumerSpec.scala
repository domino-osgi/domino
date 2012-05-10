package org.helgoboss.commons_scala_osgi

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.framework.BundleContext

@RunWith(classOf[JUnitRunner])
class OsgiConsumerSpec extends WordSpec with ShouldMatchers {
    
    trait MyService {
        def doIt
    }
    
    object MyBundleActivator extends OsgiConsumer {
        /* Define permanent OSGi service */
        def myService = requiredService[MyService]
        def myService2 = requiredService[MyService]("(myProp=myValue)")
        
        override def start(context: BundleContext) {
            super.start(context)
            
            /* Use permanent OSGi service. Would throw an exception if OSGi service is not available. */
            myService.doIt
            
            /* Use primary OSGi service of a type */
            optionalService[MyService].foreach(_.doIt)
            optionalService[MyService]("(myProp=myValue)").foreach(_.doIt)
            
            /* Get all existing OSGi services of one type and do something on them */
            services[MyService].foreach(_.doIt)
            services[MyService]("(myProp=myValue)").foreach(_.doIt)
        }
    }
    
}