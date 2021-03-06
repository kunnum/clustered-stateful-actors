lazy val akkaHttpVersion = "10.1.10"
lazy val akkaVersion     = "2.6.4"
lazy val logbackVersion  = "1.2.3"
lazy val akkaManagementVersion = "1.0.6"
lazy val akkaCassandraVersion  = "1.0.0-RC2"
lazy val jacksonVersion  = "3.6.6"
lazy val akkaEnhancementsVersion = "1.1.13"

name := "shopping-cart"
version in ThisBuild := "0.1.0"
organization in ThisBuild := "com.example"
scalaVersion in ThisBuild := "2.13.1"

def ossDependencies : Seq[ModuleID] = {
  Seq(
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % akkaCassandraVersion,
    //"org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
    "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
    "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
    "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
    "org.json4s" %% "json4s-jackson" % jacksonVersion,
    "org.json4s" %% "json4s-core" % jacksonVersion,

    //Logback
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,

    // testing
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,

    "commons-io" % "commons-io" % "2.4" % Test,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test
  )
}

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(MultiJvmPlugin).configs(MultiJvm)
  .settings(
    dockerBaseImage := "adoptopenjdk/openjdk8",
    packageName in Docker := "project-wuhu/shopping-cart",
    dockerRepository := Some("gcr.io"),
    libraryDependencies ++= {
      ossDependencies
    },
    javaOptions in Universal ++= Seq(
      "-Dcom.sun.management.jmxremote.port=8090 " +
        "-Dcom.sun.management.jmxremote.rmi.port=8090 " +
        "-Djava.rmi.server.hostname=127.0.0.1 " +
        "-Dcom.sun.management.jmxremote.authenticate=false " +
        "-Dcom.sun.management.jmxremote.ssl=false"
    )
  )
  //.settings(
  //  dockerBaseImage := "openjdk:8-slim",
  //  dockerExposedPorts ++= Seq(9200)
  //)

cinnamon := false

fork := true
