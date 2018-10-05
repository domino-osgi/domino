import mill._
import mill.scalalib._
import mill.define.Task
import mill.scalalib.publish._
import ammonite.ops.up
import $ivy.`de.tobiasroeser::mill-osgi:0.0.1-SNAPSHOT`, de.tobiasroeser.mill.osgi._

val scalaVersions = Seq("2.12.7", "2.11.12", "2.10.7")

def build() = T.command {
  domino(scalaVersions.head).jar()
}

/**
 * Build JARs for all cross Scala versions.
 */
def buildAll() = T.command {
  Task.traverse(scalaVersions)(v => domino(v).jar)
}

def test() = T.command {
  domino(scalaVersions.head).test.test()()
}

/**
 * Run tests against all cross Scala versions.
 */
def testAll() = T.command {
  Task.traverse(scalaVersions)(v => domino(v).test.test())
}

object domino extends Cross[DominoModule](scalaVersions: _*)

class DominoModule(val crossScalaVersion: String)
  extends CrossSbtModule
  with PublishModule
  with OsgiBundleModule {

  def publishVersion = "1.1.4-SNAPSHOT"

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

  def ivyDeps = Agg(
    Deps.scalaLibrary,
    Deps.scalaReflect,
    Deps.osgiCore,
    Deps.osgiCompendium,
    Deps.slf4j
  )

  object test extends Tests {

    def ivyDeps = Agg(
      Deps.scalaTest,
      Deps.felixConfigAdmin,
      Deps.pojosr,
      Deps.logbackClassic
    )

    def testFrameworks = Seq("org.scalatest.tools.Framework")

  }

  def pomSettings = PomSettings(
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
