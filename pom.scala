import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable._

val dominoVersion = "1.1.3-SNAPSHOT"

implicit val scalaVersion = System.getenv("SCALA_VERSION") match {
  case null => ScalaVersion("2.12.3")
  case v => ScalaVersion(v)
}
println("Using Scala version: " + scalaVersion.version)

val url = "https://github.com/domino-osgi/domino"

object Deps {
  // compile dependencies
  val scalaLibrary = "org.scala-lang" % "scala-library" % scalaVersion.version
  val scalaReflect = "org.scala-lang" % "scala-reflect" % scalaVersion.version
  val osgiCore = "org.osgi" % "org.osgi.core" % "4.3.0"
  val osgiCompendium = "org.osgi" % "org.osgi.compendium" % "4.3.0"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"
  val felixConfigAdmin = "org.apache.felix" % "org.apache.felix.configadmin" % "1.8.8"
  val pojosr = "com.googlecode.pojosr" % "de.kalpatec.pojosr.framework.bare" % "0.2.1"
  // val pojosr = "com.googlecode.pojosr" % "de.kalpatec.pojosr.framework.bare" % "0.3.0-SNAPSHOT"
  val bndlib = "biz.aQute.bnd" % "biz.aQute.bndlib" % "3.5.0"
}

object Plugins {
  val bnd = "biz.aQute.bnd" % "bnd-maven-plugin" % "3.5.0"
  val bundle = "org.apache.felix" % "maven-bundle-plugin" % "3.3.0"
  val jar = "org.apache.maven.plugins" % "maven-jar-plugin" % "2.5"
  val surefire = "org.apache.maven.plugins" % "maven-surefire-plugin" % "2.17"
  val scalatest = "org.scalatest" % "scalatest-maven-plugin" % "1.0"
  val scala = "net.alchim31.maven" % "scala-maven-plugin" % "3.3.1"
  val gpg = "org.apache.maven.plugins" % "maven-gpg-plugin" % "1.6"
}

Model(
  gav = "com.github.domino-osgi" %% "domino" % dominoVersion,
  modelVersion = "4.0.0",
  packaging = "bundle",
  properties = Map(
    "maven.compiler.source" -> "1.6",
    "maven.compiler.target" -> "1.6",
    "project.build.sourceEncoding" -> "UTF-8",
    "scalaBinVersion" -> scalaVersion.binaryVersion,
    "bundle.symbolicName" -> "${project.artifactId}",
    "bundle.namespace" -> "domino"
  ),
  name = "Domino for Scala " + scalaVersion.binaryVersion,
  description = "A lightweight Scala library for writing elegant OSGi bundle activators",
  url = url,
  scm = Scm(
    url = url,
    connection = "scm:git:" + url,
    developerConnection = "scm:git:" + url
  ),
  licenses = Seq(License(
    name = "MIT License",
    url = "http://www.opensource.org/licenses/mit-license",
    distribution = "repo"
  )),
  developers = Seq(
    Developer(
      name = "Benjamin Klum",
      email = "benjamin.klum@helgoboss.org"
    ),
    Developer(
      name = "Tobias Roeser",
      email = "le.petit.fou@web.de"
    )
  ),
  dependencies = Seq(
    // compile dependencies
    Deps.scalaLibrary,
    Deps.scalaReflect,
    Deps.osgiCore,
    Deps.osgiCompendium,
    // test dependencies
    Deps.scalaTest % "test",
    Deps.felixConfigAdmin % "test",
    Deps.pojosr % "test"
  ),
  build = Build(
    outputDirectory = "${project.build.directory}/classes_" + scalaVersion.binaryVersion,
    resources = Seq(
      Resource(
        directory = "src/main/resources"
      ),
      Resource(
        directory = ".",
        includes = Seq(
          "README.adoc",
          "FAQ.adoc",
          "UserGuide.adoc",
          "LICENSE"
        )
      )
    ),
    plugins = Seq(
      Plugin(
        Plugins.bundle,
        dependencies = Seq(
          Deps.bndlib
        ),
        extensions = true,
        configuration = Config(
          instructions = Config(
            _include = "osgi.bnd"
          )
        ),
        executions = Seq(Execution(phase = "verify", goals = Seq("baseline")))
      ),
      Plugin(
        Plugins.surefire,
        configuration = Config(
          skipTests = true
        )
      ),
      Plugin(
        Plugins.scalatest,
        executions = Seq(Execution(id = "test", goals = Seq("test"))),
        configuration = Config(
          reportsDirectory = "${project.build.directory}/surefire-reports",
          junitxml = "."
        )
      ),
      Plugin(
        Plugins.scala,
        executions = Seq(Execution(goals = Seq("add-source", "compile", "testCompile"))),
        configuration = Config(
          scalaVersion = scalaVersion.version,
          fork = true,
          checkMultipleScalaVersions = false,
          args = Config(
            arg = "-deprecation",
            arg = "-feature",
            arg = "-language:postfixOps"
          )
        )
      )
    )
  ),
  profiles = Seq(
    Profile(
      id = "deploy",
      build = Build(
        plugins = Seq(
          Plugin(
            Plugins.gpg,
            configuration = Config(
              repositoryId = "ossrh",
              url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
          )
        )
      )
    )
  )
)
