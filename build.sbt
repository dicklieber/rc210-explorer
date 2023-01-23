
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
      "-Xfatal-warnings"
    )

  )
Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"

resolvers += ("Reposilite" at "http://194.113.64.105:8080/releases").withAllowInsecureProtocol(true)


val logbackVersion = "1.4.5"


//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= Seq(
  guice,
  "net.wa9nnn" %% "util" % "0.1.11",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "org.specs2" %% "specs2-core" % "4.17.0" % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",

  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "commons-io" % "commons-io" % "2.11.0",
//  "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
)



// Adds additional packages into Twirl
//TwirlKeys.templateImports += "net.wa9nnn.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "net.wa9nnn.binders._"
