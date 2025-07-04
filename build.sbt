ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

ThisBuild / fork := true // Needed for ScalaFX and Akka to work well together

lazy val root = (project in file("."))
  .settings(
    name := "ProgressQuest"
  )
//  Compatible with JavaFX 21 / Java 21
libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5"
libraryDependencies += "com.lihaoyi" %% "upickle" % "4.0.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test




