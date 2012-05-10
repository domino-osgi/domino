package org.helgoboss.commons_scala_osgi

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import org.helgoboss.commons_scala_osgi.metatype._

@RunWith(classOf[JUnitRunner])
class OsgiMetaTypeProviderSpec extends WordSpec with ShouldMatchers with OsgiMetaTypeProvider {


    val objectClass = ObjectClass (id = "efed.de", requiredAttributes = List (ElementaryAttribute[Int](id = "size")))
    
    
    "An OsgiMetaTypeProvider" should {
        "be able to provide an OSGi metatype by passing an ObjectClassDefinition" in {
            whenBundleActive {
                provideMetaType(objectClass)
            }
        }
    }
}