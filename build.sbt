val AkkaVersion = "2.5.6"
val AkkaHttpVersion = "10.0.10"

lazy val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % Provided

lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
lazy val akkaActor = "com.typesafe.akka" %% "akka-actor"  % AkkaVersion

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test


scalaVersion := "2.12.3"

organization := "al.challenge"
name := "ticket-reservation-system"
version := "1.0"

libraryDependencies ++= Seq(
  macwire, akkaHttp, akkaActor, scalaTest, akkaTestKit
)

