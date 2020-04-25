lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion    = "2.6.4"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "e-treasury.io",
      scalaVersion    := "2.13.1"
    )),
    name := "clustered-stateful-actors",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                           % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"                    % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"                         % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed"              % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed"                  % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson"          % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-cassandra"          % "1.0.0-RC1",
      "ch.qos.logback"    % "logback-classic"                      % "1.2.3",
      "com.typesafe.akka" %% "akka-http-testkit"                   % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed"            % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                           % "3.0.8"         % Test
    )
  )
