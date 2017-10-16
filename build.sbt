val AkkaVersion = "2.5.6"
val AkkaHttpVersion = "10.0.10"
val AkkaHttpCirceVersion = "1.18.0"
val AkkaPersistenceCassandraVersion = "0.58"

lazy val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % Provided

lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % AkkaVersion
lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
lazy val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % AkkaVersion
lazy val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % AkkaPersistenceCassandraVersion
lazy val akkaPersistenceCassandraLauncher = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % AkkaPersistenceCassandraVersion
lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
lazy val akkaHttpJsonCirce = "de.heikoseeberger" %% "akka-http-circe" % AkkaHttpCirceVersion
lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.8.0"


lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test
lazy val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test
lazy val akkaPersistenceInMemory = "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.1.1" % Test

scalaVersion := "2.12.3"

organization := "al.challenge"
name := "ticket-reservation-system"
version := "1.0"

libraryDependencies ++= Seq(
  macwire,
  akkaActor, 
  akkaStream,
  akkaPersistence,
  akkaPersistenceCassandra,
  akkaPersistenceCassandraLauncher,
  akkaHttp,
  akkaHttpJsonCirce,
  circeGeneric,
  scalaTest,
  akkaTestKit,
  akkaHttpTestKit,
  akkaPersistenceInMemory
)

