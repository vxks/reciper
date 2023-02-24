ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val backend = (project in file("backend"))
  .settings(
    name := "reciper-backend",
    libraryDependencies := Seq(
      "dev.zio" %% "zio"         % "2.0.9",
      "dev.zio" %% "zio-http"    % "0.0.4",
      "dev.zio" %% "zio-json"    % "0.4.2",
      "dev.zio" %% "zio-streams" % "2.0.9"
    )
  )
