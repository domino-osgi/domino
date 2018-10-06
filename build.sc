import mill._
import mill.scalalib._
import mill.define.Task
import mill.scalalib.publish._
import ammonite.ops.up
import $ivy.`de.tobiasroeser::mill-osgi:0.0.1-SNAPSHOT`, de.tobiasroeser.mill.osgi._
// somehow, mill does not resolve this transitive dependency
import $ivy.`org.slf4j:slf4j-api:1.7.25`
import scala.collection.immutable.Seq

val scalaVersions = Seq("2.12.7", "2.11.12", "2.10.7")
val dominoVersion = "1.1.4-SNAPSHOT"

def _build() = T.command {
  domino(scalaVersions.head).jar()
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
  Task.traverse(scalaVersions)(v => domino(v).test.test())
}

object domino extends Cross[DominoModule](scalaVersions: _*)

class DominoModule(val crossScalaVersion: String)
  extends CrossSbtModule
  with PublishModule
  with OsgiBundleModule {

  object Deps {
    val scalaLibrary = ivy"org.scala-lang:scala-library:${crossScalaVersion}"
    val scalaReflect = ivy"org.scala-lang:scala-reflect:${crossScalaVersion}"
    val osgiCore = ivy"org.osgi:org.osgi.core:4.3.0"
    val osgiCompendium = ivy"org.osgi:org.osgi.compendium:4.3.0"
    val slf4j = ivy"org.slf4j:slf4j-api:1.7.25"
    val scalaTest = ivy"org.scalatest::scalatest:3.0.1"
    val felixConfigAdmin = ivy"org.apache.felix:org.apache.felix.configadmin:1.8.8"
    val pojosr = ivy"com.googlecode.pojosr:de.kalpatec.pojosr.framework.bare:0.2.1"
    val bndlib = ivy"biz.aQute.bnd:biz.aQute.bndlib:3.5.0"
    val logbackClassic = ivy"ch.qos.logback:logback-classic:1.1.3"
  }

  val millSourcePath = super.millSourcePath / up

  override def ivyDeps = Agg(
    Deps.scalaLibrary,
    Deps.scalaReflect,
    Deps.osgiCore,
    Deps.osgiCompendium,
    Deps.slf4j
  )
  
  override def osgiHeaders = T {
    val scalaBinVersion = crossScalaVersion.split("[.]").take(2).mkString(".")
    val namespace = "domino"
    super.osgiHeaders().copy(
      `Bundle-Name` = Some(s"Domino for Scala ${scalaBinVersion}"),
      `Import-Package` = Seq(
        s"""scala.*;version="[${scalaBinVersion},${scalaBinVersion}.50)"""",
        "org.slf4j.*;resolution:=optional",
        "*"
      ),
      `Private-Package` = Seq(
        s"${namespace}.logging.internal"
      ),
      `Export-Package` = Seq(
        s"""${namespace};version="3.0.0"""",
        s"""${namespace}.bundle_watching;version="2.0.0"""",
        s"""${namespace}.capsule;version="1.1.0"""",
        s"""${namespace}.configuration_watching;version="2.0.0"""",
        s"""${namespace}.logging;version="1.1.0"""",
        s"""${namespace}.scala_logging;version="1.1.0"""",
        s"""${namespace}.scala_osgi_metatype;version="1.1.0"""",
        s"""${namespace}.scala_osgi_metatype.adapters;version="1.1.0"""",
        s"""${namespace}.scala_osgi_metatype.builders;version="1.1.0"""",
        s"""${namespace}.scala_osgi_metatype.interfaces;version="1.2.0"""",
        s"""${namespace}.service_consuming;version="1.1.0"""",
        s"""${namespace}.service_providing;version="2.1.0"""",
        s"""${namespace}.service_watching;version="2.0.0""""
      )
    )
  }

  override def publishVersion = dominoVersion

  object test extends Tests {

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
