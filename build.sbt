import play.sbt.routes.RoutesKeys.routesImport

name := """rc210-explorer"""
organization := "net.wa9nnn"


enablePlugins(JavaServerAppPackaging)

lazy val root = (project in file(".")).enablePlugins(PlayScala, BuildInfoPlugin)
  .settings(
    //    Compile / mappings := Seq(),
    //    packageDoc / mappings := Seq(),
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
      //      "-Xfatal-warnings",
      //      "-Ymacro-annotations",
      "-Xlog-implicits"
    )

  )
Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"

resolvers += ("Reposilite" at "http://194.113.64.105:8080/releases").withAllowInsecureProtocol(true)


val logbackVersion = "1.4.7"
val specs2Version = "4.19.2"
libraryDependencies += specs2 % Test

val akkaVersion = "2.6.20"
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= Seq(
  guice, specs2 % Test,
  "net.wa9nnn" %% "util" % "0.1.14",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "net.codingwell" %% "scala-guice" % "5.1.1",
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "commons-io" % "commons-io" % "2.11.0",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.fazecast" % "jSerialComm" % "2.9.3",
  //  "io.suzaku" %% "boopickle" % "1.4.0",
  "org.fusesource.jansi" % "jansi" % "2.4.0",
  "org.apache.commons" % "commons-text" % "1.10.0",
  "com.github.kxbmap" %% "configs" % "0.6.1",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.github.andyglow" %% "typesafe-config-scala" % "2.0.0" % Compile,

)


routesImport += "net.wa9nnn.rc210.Binders._"
routesImport += "net.wa9nnn.rc210.data.FieldKey"
routesImport += "net.wa9nnn.rc210.key.KeyFactory._"
routesImport += "net.wa9nnn.rc210.key.KeyKind"