
name := """rc210-explorer"""
organization := "net.wa9nnn"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, BuildInfoPlugin)
  .settings(
    scalaVersion := "2.13.10",

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

    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-Ymacro-annotations",
      "-Xlog-implicits"
    )

  )
Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"

resolvers += ("Reposilite" at "http://194.113.64.105:8080/releases").withAllowInsecureProtocol(true)


val logbackVersion = "1.4.5"
val specs2Version = "4.19.2"
libraryDependencies += specs2 % Test


//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= Seq(
  guice, specs2 % Test,
  "net.wa9nnn" %% "util" % "0.1.12-SNAPSHOT",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "net.codingwell" %% "scala-guice" % "5.1.0",
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "commons-io" % "commons-io" % "2.11.0",
  "com.fazecast" % "jSerialComm" % "[2.0.0,3.0.0)",
  "io.suzaku" %% "boopickle" % "1.4.0",
  "org.fusesource.jansi" % "jansi" % "2.4.0",
  "org.apache.commons" % "commons-text" % "1.10.0",
)


//routesImport ++= Seq(
//  "net.wa9nnn.rc210.Key",
//  "net.wa9nnn.rc210.KeyFormats._",
//  "net.wa9nnn.rc210.data.FieldKey._",
//)