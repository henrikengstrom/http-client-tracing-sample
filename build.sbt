lazy val akkaHttpVersion = "10.1.5"
lazy val akkaVersion    = "2.5.17"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.6"
    )),
    name := "akka-http-quickstart-scala",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      Cinnamon.library.cinnamonOpenTracingZipkin,
      "io.opentracing"    %  "opentracing-api"      % "0.23.0"
    )
  ).enablePlugins(Cinnamon)

cinnamon in run := true