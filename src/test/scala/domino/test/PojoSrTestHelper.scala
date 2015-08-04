package domino.test

import java.util.HashMap
import java.util.ServiceLoader
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistryFactory
import org.osgi.framework.BundleActivator
import de.kalpatec.pojosr.framework.PojoSR

trait PojoSrTestHelper {

  def withPojoServiceRegistry[T](f: PojoServiceRegistry => T) = {
    //    val loader = ServiceLoader.load(classOf[PojoServiceRegistryFactory])
    //    val registry = loader.iterator().next().newPojoServiceRegistry(new HashMap())
    val registry = new PojoSR(new HashMap())
    f(registry)
  }

  def withStartedBundle(activator: BundleActivator): Unit =
    withPojoServiceRegistry { sr =>
      withStartedBundle(sr)(activator)
    }

  def withStartedBundle(sr: PojoServiceRegistry)(activator: BundleActivator): Unit = {
    activator.start(sr.getBundleContext())
    activator.stop(sr.getBundleContext())
  }

}