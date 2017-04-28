import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable._

val dominoVersion = "1.1.2"

implicit val scalaVersion = System.getenv("SCALA_VERSION") match {
  case null => ScalaVersion("2.12.2")
  case v => ScalaVersion(v)
}
println("Using Scala version: " + scalaVersion.version)

val url = "https://github.com/domino-osgi/domino"
val pojosr = "com.googlecode.pojosr" % "de.kalpatec.pojosr.framework.bare" % "0.2.1"
// val pojosr = "com.googlecode.pojosr" % "de.kalpatec.pojosr.framework.bare" % "0.3.0-SNAPSHOT"

ScalaModel(
  gav = "com.github.domino-osgi" %% "domino" % dominoVersion,
  modelVersion = "4.0.0",
  packaging = "bundle",
  prerequisites = Prerequisites(
    maven = "3.3.1"
  ),
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
    "org.scala-lang" % "scala-library" % scalaVersion.version,
    "org.scala-lang" % "scala-reflect" % scalaVersion.version,
    "org.osgi" % "org.osgi.core" % "4.3.0",
    "org.osgi" % "org.osgi.compendium" % "4.3.0",
    // test dependencies
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.apache.felix" % "org.apache.felix.configadmin" % "1.8.8" % "test",
    pojosr % "test"
  ),
  build = Build(
    outputDirectory = "${project.build.directory}/classes_" + scalaVersion.binaryVersion,
    plugins = Seq(
      Plugin(
        "org.apache.felix" % "maven-bundle-plugin" % "2.5.4",
        extensions = true,
        configuration = Config(
          instructions = Config(
            _include = "osgi.bnd"
          )
        ),
        executions = Seq(Execution(phase = "verify", goals = Seq("baseline")))
      ),
      Plugin(
        "org.apache.maven.plugins" % "maven-jar-plugin" % "2.5",
        configuration = Config(
          archive = Config(
            addMavenDescriptor = false
          )
        )
      ),
      Plugin(
        "org.apache.maven.plugins" % "maven-surefire-plugin" % "2.17",
        configuration = Config(
          skipTests = true
        )
      ),
      Plugin(
        "org.scalatest" % "scalatest-maven-plugin" % "1.0",
        executions = Seq(Execution(id = "test", goals = Seq("test"))),
        configuration = Config(
          reportsDirectory = "${project.build.directory}/surefire-reports",
          junitxml = "."
        )
      ),
      Plugin(
        "net.alchim31.maven" % "scala-maven-plugin" % "3.2.1",
        executions = Seq(Execution(goals = Seq("compile", "testCompile"))),
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
            "org.apache.maven.plugins" % "maven-gpg-plugin" % "1.6",
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
