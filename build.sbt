enablePlugins(ScoverageSbtPlugin)

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / fork := true
assembly / assemblyJarName := s"${name.value}-${version.value}.jar"
assembly / assemblyMergeStrategy := {
  case "reference.conf" => MergeStrategy.concat // Concatenate all reference.conf files
  case PathList("META-INF", xs@_*) => MergeStrategy.discard // Discard all META-INF files
  case _ => MergeStrategy.first // For all others, take the first occurrence
}

assembly / assemblyExcludedJars := {
  val cp = (assembly / fullClasspath).value
  cp filter { jar =>
    jar.data.getName.contains("scoverage") // Exclude any jars related to scoverage (code coverage tool)
  }
}

lazy val root = (project in file("."))
  .settings(
    name := "ProgressQuest",
    scalafmtOnCompile := true,
    coverageEnabled := true,
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
      "com.lihaoyi" %% "upickle" % "3.1.0",
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
    )
  )
