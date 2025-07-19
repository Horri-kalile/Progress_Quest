enablePlugins(ScoverageSbtPlugin)

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / fork := true

// Automatic JavaFX dependency handling
lazy val javaFXVersion = "21.0.8"
lazy val osName = System.getProperty("os.name").toLowerCase match {
  case n if n.contains("linux") => "linux"
  case n if n.contains("mac") => "mac"
  case n if n.contains("win") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val root = (project in file("."))
  .settings(
    name := "ProgressQuest",
    scalafmtOnCompile := true,
    coverageEnabled := true,

    // Assembly settings
    assembly / assemblyJarName := s"${name.value}-${version.value}.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case PathList("META-INF", "services", _*) => MergeStrategy.concat
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    },
    assembly / mainClass := Some("GameMain"),
    assembly / assemblyOption := (assembly / assemblyOption).value
      .withIncludeScala(true)
      .withIncludeDependency(true),

    // Main dependencies
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "com.lihaoyi" %% "upickle" % "3.1.0",
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
    ),

    // Automatic JavaFX modules
    libraryDependencies ++= Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
      .map(m => "org.openjfx" % s"javafx-$m" % javaFXVersion classifier osName),

    // Runtime configuration
    Compile / mainClass := Some("GameMain"),
    run / javaOptions ++= Seq(
      "--module-path", (Compile / dependencyClasspath).value
        .filter(_.data.getName.contains("javafx"))
        .map(_.data.getAbsolutePath)
        .mkString(System.getProperty("path.separator")),
      "--add-modules=javafx.controls,javafx.fxml",
      "-Dprism.order=sw"
    ),
    run / fork := true
  )