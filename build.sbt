enablePlugins(ScoverageSbtPlugin)

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / fork := true

lazy val root = (project in file("."))
  .settings(
    name := "ProgressQuest",

    coverageMinimum := 50,
    coverageFailOnMinimum := false,

    scalafmtOnCompile := true,

    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
      "com.lihaoyi" %% "upickle" % "3.1.0",
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
    )
  )
