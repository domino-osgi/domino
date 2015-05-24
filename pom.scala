import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable._


implicit val scalaVersion = System.getenv("SCALA_VERSION") match {
  case null => ScalaVersion("2.10.5")
  case v => ScalaVersion(v)
}
println("Using Scala version: " + scalaVersion.version)

val url = "https:/github.com/domino-osgi/domino"

ScalaModel(
  gav = "com.github.domino-osgi" %% "domino" % "1.0.1-SNAPSHOT",
  modelVersion = "4.0.0",
  packaging = "bundle",
  prerequisites = Prerequisites(
    maven = "3.3.1"
  ),
  properties = Map(
    "maven.compiler.source" -> "1.6",
    "maven.compiler.target" -> "1.6",
    "project.build.sourceEncoding" -> "UTF-8",
    "scalaBinVersion" -> scalaVersion.binaryVersion
  ),
  name = "Domino",
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
    // "org.helgoboss" % "scala-osgi-metatype" % "1.0.0",
    // "org.helgoboss" % "scala-logging" % "1.0.0",
    // "org.helgoboss" % "capsule" % "1.1.0",
    // test dependencies
    // "junit" % "junit" % "4.11" % "test",
    "org.scalatest" %% "scalatest" % "2.2.0" % "test"
  ),
  build = Build(
    plugins = Seq(
      Plugin(
        "org.apache.felix" % "maven-bundle-plugin" % "2.5.0",
        extensions = true,
        configuration = Config(
          instructions = Config(
            _include = "osgi.bnd"
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
      )
    )
  )
)
