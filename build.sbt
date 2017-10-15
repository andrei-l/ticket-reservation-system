val AkkaVersion = "2.4.19"
val AkkaHttpVersion = "10.0.9"
val AkkaHttpCirceVersion = "1.18.0"

lazy val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % Provided

lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % AkkaVersion
lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
lazy val akkaHttpJsonCirce = "de.heikoseeberger" %% "akka-http-circe" % AkkaHttpCirceVersion
lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.8.0"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test
lazy val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test

scalaVersion := "2.12.3"

organization := "al.challenge"
name := "ticket-reservation-system"
version := "1.0"

libraryDependencies ++= Seq(
  macwire, akkaActor, akkaHttp, akkaHttpJsonCirce, circeGeneric, scalaTest, akkaTestKit, akkaHttpTestKit
)

