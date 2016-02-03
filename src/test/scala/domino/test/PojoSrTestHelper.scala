package domino.test

import java.io.File

import scala.collection.JavaConverters.mapAsJavaMapConverter

import org.osgi.framework.BundleActivator

import de.kalpatec.pojosr.framework.PojoSR
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry

object PojoSrTestHelper {

  val OnlyOnePojoSrAtATime = new Object()

}

trait PojoSrTestHelper {

  import PojoSrTestHelper._

  def withPojoServiceRegistry[T](f: PojoServiceRegistry => T) = OnlyOnePojoSrAtATime.synchronized {
    val dir = File.createTempFile("pojosr-", "")
    dir.delete()
    dir.mkdirs()
    try {
      System.setProperty("org.osgi.framework.storage", dir.getAbsolutePath())
      val registry = new PojoSR(Map("felix.cm.dir" -> dir.getAbsolutePath()).asJava)
      f(registry)
    } finally {
      System.clearProperty("org.osgi.framework.storage")
      deleteRecursive(dir)
    }
  }

  def deleteRecursive(files: File*): Unit = files.map { file =>
    if (file.isDirectory) deleteRecursive(file.listFiles: _*)
    file.delete match {
      case false if file.exists =>
        throw new RuntimeException(s"Could not delete ${if (file.isDirectory) "dir" else "file"}: ${file}")
      case _ =>
    }
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