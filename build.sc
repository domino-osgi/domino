import ammonite.ops._
import mill._
import mill.define.Task
import mill.scalalib._
import mill.scalalib.publish._

import $ivy.`de.tototec::de.tobiasroeser.mill.osgi:0.2.0`
import de.tobiasroeser.mill.osgi._

import $ivy.`de.tototec::de.tobiasroeser.mill.publishM2:0.1.3`
import de.tobiasroeser.mill.publishM2._

val scalaVersions = Seq("2.13.2", "2.12.11", "2.11.12", "2.10.7")
val dominoVersion = "1.1.4-SNAPSHOT"

def _all() = T.command {
  _buildAll()()
  _testAll()()
}

def _build() = T.command {
  domino(scalaVersions.head).osgiBundle()
}

/** Build JARs for all cross Scala versions. */
def _buildAll() = T.command {
  Task.traverse(scalaVersions)(v => domino(v).osgiBundle)
}

def _test() = T.command {
  domino(scalaVersions.head).test.test()()
}

/** Run tests against all cross Scala versions. */
def _testAll() = T.command {
  // T.ctx().log.info("Test all")
  // T.ctx().log.debug("Running all tests")
  Task.traverse(scalaVersions)(v => domino(v).test.test())
}

 object Deps {
  val osgiCore = ivy"org.osgi:org.osgi.core:4.3.0"
  val osgiCompendium = ivy"org.osgi:org.osgi.compendium:4.3.0"
  val slf4j = ivy"org.slf4j:slf4j-api:1.7.25"
  val scalaTest = ivy"org.scalatest::scalatest:3.0.8"
  val felixConfigAdmin = ivy"org.apache.felix:org.apache.felix.configadmin:1.8.8"
  val pojosr = ivy"com.googlecode.pojosr:de.kalpatec.pojosr.framework.bare:0.2.1"
  val bndlib = ivy"biz.aQute.bnd:biz.aQute.bndlib:4.0.0"
  val bndLauncher = ivy"biz.aQute.bnd:biz.aQute.launcher:4.0.0"
  val logbackClassic = ivy"ch.qos.logback:logback-classic:1.1.3"
}

object domino extends Cross[DominoModule](scalaVersions: _*)

class DominoModule(override val crossScalaVersion: String)
  extends CrossScalaModule
  with PublishModule
  with PublishM2Module
  with OsgiBundleModule { dominoModule =>

  override def skipIdea = crossScalaVersion != scalaVersions.head

  override val millSourcePath = super.millSourcePath / up / 'src / 'main
  override def sources = T.sources { millSourcePath / 'scala }
  override def resources = T.sources { millSourcePath / 'resources }

  val scalaBinVersion = crossScalaVersion.split("[.]").take(2).mkString(".")
  override def artifactName = "domino"

  def scalaReflectIvyDeps = T{
    if(mill.scalalib.api.Util.isDotty(crossScalaVersion))
        Agg.empty[Dep]
      else
        Agg(
          ivy"$scalaOrganization:scala-reflect:$scalaVersion".forceVersion()
        )
  }

  override def ivyDeps = T{
    scalaLibraryIvyDeps() ++ scalaReflectIvyDeps() ++ Agg(
    Deps.osgiCore,
    Deps.osgiCompendium,
    Deps.slf4j
  )}

  override def osgiHeaders = T {
    super.osgiHeaders().copy(
      `Bundle-Name` = Some(s"Domino for Scala ${scalaBinVersion}"),
      `Bundle-Activator` = Some("domino.internal.DominoBundleActivator"),
      `Import-Package` = Seq(
        s"""scala.*;version="[${scalaBinVersion},${scalaBinVersion}.50)"""",
        "org.slf4j.*;resolution:=optional",
        "*"
      ),
      `Private-Package` = Seq( //        "domino.logging.internal"
      ),
      `Export-Package` = Seq(
        """domino;version="3.0.0"""",
        """domino.bundle_watching;version="2.0.0"""",
        """domino.capsule;version="1.1.0"""",
        """domino.configuration_watching;version="2.0.0"""",
        """domino.logging;version="1.1.0"""",
        """domino.scala_logging;version="1.1.0"""",
        """domino.scala_osgi_metatype;version="1.1.0"""",
        """domino.scala_osgi_metatype.adapters;version="1.1.0"""",
        """domino.scala_osgi_metatype.builders;version="1.1.0"""",
        """domino.scala_osgi_metatype.interfaces;version="1.2.0"""",
        """domino.service_consuming;version="1.1.0"""",
        """domino.service_providing;version="2.1.0"""",
        """domino.service_watching;version="2.0.1"""",
        """domino.service_watching.monitor;version="1.0.0""""
      )
    )
  }

  override def publishVersion = dominoVersion

  object test extends Tests {
    override def skipIdea = dominoModule.skipIdea

    override val millSourcePath = dominoModule.millSourcePath / up / 'test
    override def sources = T.sources { millSourcePath / 'scala }
    override def resources = T.sources { millSourcePath / 'resources }

    override def ivyDeps = Agg(
      Deps.scalaTest,
      Deps.felixConfigAdmin,
      Deps.pojosr,
      Deps.logbackClassic
    )

    override def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

  override def pomSettings = PomSettings(
    description = "A lightweight Scala library for writing elegant OSGi bundle activators",
    organization = "com.github.domino-osgi",
    url = "https://github.com/domino-osgi/domino",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("domino-osgi", "domino"),
    developers = Seq(
      Developer("lefou", "Tobias Roeser", "https://github.com/lefou"),
      Developer("helgoboss", "Benjamin Klum", "benjamin.klum@helgoboss.org")
    )
  )

}

object integrationTest extends Module {

  object support extends ScalaModule {
    override def scalaVersion = scalaVersions.head
  }

  trait TestCase extends JavaModule {

  }

  object test1 extends TestCase {
    override def ivyDeps = Agg(
      Deps.bndlib,
      Deps.bndLauncher,
      Deps.slf4j,
      Deps.osgiCore
    )

    override def mainClass = Some("aQute.launcher.Launcher")
  }

}
