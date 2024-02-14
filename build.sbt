import play.sbt.routes.RoutesKeys.routesImport

name := """rc210-explorer"""
organization := "net.wa9nnn"
maintainer := "dick@u50.com"

enablePlugins(JavaServerAppPackaging)

lazy val root = (project in file(".")).enablePlugins(PlayScala, BuildInfoPlugin)
  .settings(
    //    Compile / mappings := Seq(),
    //    packageDoc / mappings := Seq(),
    //    scalaVersion := "2.13.10",
    scalaVersion := "3.3.1",
    buildInfoKeys :=
      Seq[BuildInfoKey](
        name, version, scalaVersion, sbtVersion,
        sbtVersion, git.remoteRepo,
        git.gitCurrentBranch, git.gitHeadCommit, git.versionProperty, git.baseVersion, git.gitCurrentTags, git.remoteRepo
      ),
    buildInfoOptions := Seq(
      BuildInfoOption.BuildTime,
      BuildInfoOption.ToMap,
      BuildInfoOption.ToJson
    ),
    buildInfoPackage := "net.wa9nnn.rc210explorer",
    //    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    //    docusaurBuildDir := docusaurDir.value / "build",

    routesGenerator := InjectedRoutesGenerator,

    scalacOptions ++= Seq(
      "-feature",
      " - language: implicitConversions",
      //      "-deprecation",
      //      "-Xfatal-warnings",
      //      "-Ymacro-annotations",
    )

  )
Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"

Test / logBuffered := false
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/scalatest.html")

resolvers +=
  ("reposilite-repository-releases" at "http://repo.wa9nnn.tech:8080/releases").withAllowInsecureProtocol(true)

val logbackVersion = "1.4.11"
libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test,
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
  "com.wa9nnn" %% "wa9nnnutil" % "3.0.0.5",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "commons-io" % "commons-io" % "2.11.0",
  "com.fazecast" % "jSerialComm" % "2.10.4",
  "org.fusesource.jansi" % "jansi" % "2.4.0",
  "org.apache.commons" % "commons-text" % "1.10.0",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.github.andyglow" %% "typesafe-config-scala" % "2.0.0" % Compile,
  "com.beachape" %% "enumeratum" % "1.7.3",
  "com.beachape" %% "enumeratum-play" % "1.8.0",
)
publishTo := {
  if (isSnapshot.value)
    Some(("Reposilite Repository" at "http://repo.wa9nnn.tech:8080/snapshots").withAllowInsecureProtocol(true))
  else
    Some(("Reposilite Repository" at "http://repo.wa9nnn.tech:8080/releases").withAllowInsecureProtocol(true))
}


routesImport += "net.wa9nnn.rc210.Binders._"
routesImport += "net.wa9nnn.rc210.FieldKey"
routesImport += "net.wa9nnn.rc210.serial.CommandSendRequest"
routesImport += "net.wa9nnn.rc210.serial.CommandSendRequest.pathBinder"
routesImport += "net.wa9nnn.rc210.KeyKind"
routesImport += "net.wa9nnn.rc210.Key"
routesImport += "net.wa9nnn.rc210.ui.nav.TabKind"
routesImport += "net.wa9nnn.rc210.serial.SendField"
