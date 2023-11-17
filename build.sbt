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


    scalacOptions ++= Seq(
      "-feature",
//      "-deprecation",
      //      "-Xfatal-warnings",
      //      "-Ymacro-annotations",
    )

  )
Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"

Test / logBuffered := false
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/scalatest.html")

resolvers += ("Reposilite" at "http://194.113.64.105:8080/releases").withAllowInsecureProtocol(true)


val logbackVersion = "1.4.11"
//val specs2Version = "4.20.0"
//libraryDependencies += specs2 % Test

//val akkaVersion = "2.8.5"
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= Seq(
  guice,

  //  "org.apache.pekko" %% "akka-actor-testkit-typed" % akkaVersion % Test,

  //  "org.scalactic" %% "scalactic" % "3.2.16",
  //  "org.scalatest" %% "scalatest" % "3.2.16" % "test",
  //  "org.scalatest" %% "scalatest-flatspec" % "3.2.16" % "test",
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
  "org.scalatest" %% "scalatest" % "3.2.16" % "test",
  //  "org.scalatest" %% "scalatest-wordspec" % "3.2.16" % "test",
  "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % Test,
  //  "org.scalatestplus" %% "mockito-4-6" % " 3.2.16" % "test",
  //  "org.specs2" %% "specs2-core" % specs2Version % Test,
  //  "org.specs2" %% "specs2-matcher-extra" % specs2Version % Test,
  //  "org.specs2" % "specs2-scalaz" % specs2Version % "test",
  "net.wa9nnn" %% "util" % "0.1.15-SNAPSHOT",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "commons-io" % "commons-io" % "2.11.0",
  "com.fazecast" % "jSerialComm" % "2.9.3",
  //  "io.suzaku" %% "boopickle" % "1.4.0",
  "org.fusesource.jansi" % "jansi" % "2.4.0",
  "org.apache.commons" % "commons-text" % "1.10.0",
  //  "com.github.kxbmap" %% "configs" % "0.6.1",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.github.andyglow" %% "typesafe-config-scala" % "2.0.0" % Compile,
//  "com.beachape" %% "enumeratum" % "1.7.3",
)


routesImport += "net.wa9nnn.rc210.Binders._"
routesImport += "net.wa9nnn.rc210.data.FieldKey"
//routesImport += "net.wa9nnn.rc210.key.KeyFactory._"
//routesImport += "net.wa9nnn.rc210.key.KeyKind"
routesImport += "net.wa9nnn.rc210.Key"
routesImport += "net.wa9nnn.rc210.serial.ComPort"