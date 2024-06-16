name := """backlog-test"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0" % Test,
  "com.typesafe.play" %% "play-ahc-ws" % "2.9.3",
  "com.typesafe.play" %% "twirl-api" % "1.6.2",
  "com.typesafe.play" %% "play-json" % "2.9.3",
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
