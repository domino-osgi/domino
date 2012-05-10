package org.helgoboss.commons_scala_osgi

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.osgi.framework.BundleContext

@RunWith(classOf[JUnitRunner])
class OsgiConfigurationWatcherSpec extends WordSpec with ShouldMatchers with OsgiConfigurationWatcher {
        
    "An OsgiConfigurationWatcher" should {
        val defaultConf = Map("test1" -> 2, "test2" -> "b")
        
        "be able to execute code whenever a specific configuration object is changed" in {
            whenBundleActive {
                whenConfigurationUpdated("org.helgoboss.test") { conf =>
                    val resultConf = conf match {
                        case Some(c) => defaultConf ++ c
                        case None => defaultConf
                    }
                }
            }
        }
        
        "allow to merge actual configuration with default configuration" in {
            val actualConf = Map("test1" -> 5)
            val resultConf = defaultConf ++ actualConf
            assert(resultConf === Map("test1" -> 5, "test2" -> "b"))
        }
        
    }
    
}