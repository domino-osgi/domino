Domino
======

Domino is a small library for the programming language [Scala](http://www.scala-lang.org) designed to support developers in writing [bundle activators](http://www.osgi.org/javadoc/r4v43/core/org/osgi/framework/BundleActivator.html) for the Java module system [OSGi](http://www.osgi.org/Technology/WhyOSGi). It strives to make writing complex and highly-dynamic bundle activators as easy as possible without sacrificing the power of the OSGi API.

As such, Domino is a lightweight alternative to OSGi component models like [iPOJO](http://ipojo.org), [Blueprint](http://wiki.osgi.org/wiki/Blueprint) and [Declarative Services](http://wiki.osgi.org/wiki/Declarative_Services). Especially for those who want to leverage the power of pure code instead of reverting to an XML- or annotation-based approach.

## Examples

### Wait for service and register service 

```scala
import org.helgoboss.domino.DominoActivator
import org.osgi.service.http.HttpService

class MyService(httpService: HttpService)

class Activator extends DominoActivator {
  whenBundleActive {
    // Make service available as long as another 
    // service is present
    whenServicePresent[HttpService] { httpService =>
      val myService = new MyService(httpService)
      myService.providesService[MyService]
    }
  }
}
```

### Listen for configuration updates

```scala
import org.helgoboss.domino.DominoActivator

class KeyService(key: String)

class Activator extends DominoActivator {
  whenBundleActive {
    // Reregister service whenever configuration changes
    whenConfigurationActive("my_service") { conf =>
      val key = conf.getOrElse("key", "defaultKey")
      new KeyService(key).providesService[KeyService]
    }
  }
}
```

## Further reading

For further information, please visit the project website http://www.helgoboss.org/projects/domino/.
