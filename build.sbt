val scalaVersions = Seq("2.11.7", "2.10.6")

val namespace = "domino"

lazy val root = (project in file(".")).
  settings(
    name := "domino",
    version := "1.1.1-SNAPSHOT",
    
    scalaVersion := scalaVersions.head,
    crossScalaVersions := scalaVersions,
    
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.osgi" % "org.osgi.core" % "4.3.0",
      "org.osgi" % "org.osgi.compendium" % "4.3.0",
      // test dependencies
      "org.scalatest" %% "scalatest" % "2.2.0" % "test",
      "com.googlecode.pojosr" % "de.kalpatec.pojosr.framework.bare" % "0.2.1"
    ),
    
    sourcesInBase := false,
    
    osgiSettings,
    OsgiKeys.additionalHeaders := Map(
      "bundle.symbolicName" -> namespace,
      "bundle.namespace" -> namespace,
      "scalaBinVersion" -> scalaBinaryVersion.value,
      "-include" -> "osgi.bnd"
    )
  )

