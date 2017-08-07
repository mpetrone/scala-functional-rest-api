scalaVersion := "2.12.2" // Also supports 2.10.x and 2.12.x

organization := "com.example"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")

scalacOptions ++= Seq(
  "-feature"   // Emit warning and location for usages of features that should be imported explicitly.
)

lazy val doobieVersion = "0.4.1"
lazy val http4sVersion = "0.17.0-M3"
lazy val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "org.http4s"            %% "http4s-dsl"                 % http4sVersion,
  "org.http4s"            %% "http4s-blaze-server"        % http4sVersion,
  "org.http4s"            %% "http4s-blaze-client"        % http4sVersion,
  "org.http4s"            %% "http4s-circe"               % http4sVersion,
  "io.circe"              %% "circe-generic"              % circeVersion,
  "io.circe"              %% "circe-literal"              % circeVersion,
  "io.circe"              %% "circe-parser"               % circeVersion,
  "io.circe"              %% "circe-generic-extras"       % circeVersion,
  "io.circe"              %% "circe-java8"                % circeVersion,
  "org.scalatest"         %% "scalatest"                  % "3.0.1" % "test",
  "ch.qos.logback"         % "logback-classic"            % "1.2.3",
  "org.slf4j"              % "slf4j-api"                  % "1.7.25",
  "org.tpolecat"          %% "doobie-core-cats"           % doobieVersion,
  "org.tpolecat"          %% "doobie-postgres-cats"       % doobieVersion,
  "org.tpolecat"          %% "doobie-scalatest-cats"      % doobieVersion,
  "org.reactormonk"       %% "cryptobits"                 % "1.1"
)

addCommandAlias("hot-run", "~re-start run")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)