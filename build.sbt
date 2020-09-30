
val Http4sVersion = "0.20.0"
val CatsVersion = "2.0.0"
val ZioCatsVersion = "2.0.0.0-RC3"
val ZioVersion = "1.0.0-RC13"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    organization := "example",
    name := "ZioReportsPrototype",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.3",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "org.http4s"                   %% "http4s-blaze-server" % "0.21.7",
      "org.http4s"                   %% "http4s-circe"        % "0.21.7",
      "org.http4s"                   %% "http4s-dsl"          % "0.21.7",
      "io.circe"                     %% "circe-core"          % "0.13.0",
      "io.circe"                     %% "circe-generic"       % "0.13.0",
      "io.circe"                     %% "circe-literal"       % "0.13.0" % "test",
      "org.tpolecat"                 %% "doobie-core"         % "0.9.2",
      "org.tpolecat"                 %% "doobie-h2"           % "0.9.2",
      "org.tpolecat"                 %% "doobie-hikari"       % "0.9.2",
      "org.typelevel"                %% "jawn-parser"         % "1.0.0"  % "test",
      "dev.zio"                      %% "zio"                 % "1.0.1",
      "dev.zio"                      %% "zio-test"            % "1.0.1"  % "test",
      "dev.zio"                      %% "zio-test-sbt"        % "1.0.1"  % "test",
      "dev.zio"                      %% "zio-interop-cats"    % "2.1.4.0",
      "dev.zio"                      %% "zio-logging"         % "0.5.2",
      "dev.zio"                      %% "zio-logging-slf4j"   % "0.5.2",
      "org.flywaydb"                  % "flyway-core"         % "6.5.7",
      "com.h2database"                % "h2"                  % "1.4.200",
      "org.apache.logging.log4j"      % "log4j-api"           % "2.13.3",
      "org.apache.logging.log4j"      % "log4j-core"          % "2.13.3",
      "org.apache.logging.log4j"      % "log4j-slf4j-impl"    % "2.13.3",
      "com.github.pureconfig"        %% "pureconfig"          % "0.14.0",
      "com.lihaoyi"                  %% "sourcecode"          % "0.2.1",
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      compilerPlugin(("org.typelevel" % "kind-projector"      % "0.11.0").cross(CrossVersion.full))
    )

  )

scalacOptions := Seq(
  "-feature",
  "-deprecation",
  "-explaintypes",
  "-unchecked",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:existentials",
  "-Xfatal-warnings",
  "-Xlint:-byname-implicit,_",
  "-Ywarn-value-discard",
  "-Ywarn-numeric-widen",
  "-Ywarn-extra-implicit",
  "-Ywarn-unused"
) ++ (if (isSnapshot.value) Seq.empty else Seq("-opt:l:inline"))