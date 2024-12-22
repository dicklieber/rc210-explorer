import play.sbt.routes.RoutesKeys.{routesGenerator, routesImport}
import sbt.*
import sbt.Keys.{packageBin, *}
import sbt.internal.util.ManagedLogger
import sbtrelease.ReleaseStateTransformations.*
import scala.sys.process.*

name := """rc210-explorer"""
organization := "net.wa9nnn"
maintainer := "dick@u505.com"

enablePlugins(UniversalPlugin)

lazy val root = (project in file(".")).enablePlugins(PlayScala, BuildInfoPlugin)
  .settings(
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
    versionScheme := Some("early-semver"),

    routesGenerator := InjectedRoutesGenerator,
    //    githubWorkflowJavaVersions += JavaSpec.temurin("17"),

    scalacOptions ++= Seq(
      "-feature",
      " - language: implicitConversions",
      "-explain",
      //      "-deprecation",
      //      "-Xfatal-warnings",
      //      "-Ymacro-annotations",
    )

  )
Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"

Test / logBuffered := false
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/scalatest.html")

val logbackVersion = "1.4.11"
libraryDependencies ++= Seq(
  guice,
  "com.lihaoyi" %% "os-lib" % "0.9.3",
  "org.scalatest" %% "scalatest" % "latest.integration" % Test,
  "org.scalatest" %% "scalatest" % "latest.integration" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "latest.integration" % Test,
  "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0" % Test,
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
  "com.wa9nnn" %% "wa9nnnutil" % "3.0.0.9-SNAPSHOT",
  "org.commonmark" % "commonmark" % "0.21.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "commons-io" % "commons-io" % "2.11.0",
  "com.fazecast" % "jSerialComm" % "2.10.4",
  "org.fusesource.jansi" % "jansi" % "2.4.0",
  "org.apache.commons" % "commons-text" % "1.10.0",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.github.andyglow" %% "typesafe-config-scala" % "2.0.0",
  "com.beachape" %% "enumeratum" % "1.7.5",
  "com.beachape" %% "enumeratum-play" % "1.8.2",
  "net.codingwell" %% "scala-guice" % "6.0.0",


)
//publishTo := {
//  if (isSnapshot.value)
//    Some(("Reposilite Repository" at "http://repo.wa9nnn.tech:8080/snapshots").withAllowInsecureProtocol(true))
//  else
//    Some(("Reposilite Repository" at "http://repo.wa9nnn.tech:8080/releases").withAllowInsecureProtocol(true))
//}
publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))

//resolvers += "Nexus" at "http://localhost:8081/nexus/content/groups/public"
//resolvers += "Nexus" at "https://s01.oss.sonatype.org/"
resolvers +=
  "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

routesImport += "net.wa9nnn.rc210.Binders._"
routesImport += "net.wa9nnn.rc210.serial.UploadRequest"
routesImport += "net.wa9nnn.rc210.serial.UploadRequest.pathBinder"
routesImport += "net.wa9nnn.rc210.KeyMetadata"
routesImport += "net.wa9nnn.rc210.Key"
routesImport += "net.wa9nnn.rc210.ui.nav.TabKind"
routesImport += "net.wa9nnn.rc210.serial.SendField"

// first define a task key
lazy val ghRelease = taskKey[Unit]("Publish zip to GitHub release")

// then implement the task key
ghRelease := {
  val ver = version.value
  val cmd = s"gh release create v$ver --generate-notes /Users/dlieber/devlocal/github/rc210-explorer/target/universal/rc210-explorer-$ver.zip"
  val log: ManagedLogger = streams.value.log
  val i: Int = cmd ! (log)
  val value = s"cmd: $cmd returned $i"
}

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  inquireVersions, // : ReleaseStep
  runClean, // : ReleaseStep
  runTest, // : ReleaseStep
  setReleaseVersion, // : ReleaseStep
  ReleaseStep(releaseStepTask(Universal / packageBin)),
  releaseStepTask(ghRelease),
  commitReleaseVersion, // : ReleaseStep, performs the initial git checks
  tagRelease, // : ReleaseStep
  publishArtifacts, // : ReleaseStep, checks whether `publishTo` is properly set up
  setNextVersion, // : ReleaseStep
  commitNextVersion, // : ReleaseStep
  pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
)
