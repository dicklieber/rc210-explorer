package net.wa9nnn.rc210.fixtures

import com.typesafe.config.{Config, ConfigFactory}
import org.specs2.mutable.Specification
import configs.syntax._
import org.apache.commons.io.FileUtils

import java.io.File
import java.nio.file.{Files, Path}

class WithTestConfiguration extends Specification {

  implicit val config: Config = ConfigFactory.parseResources("test.conf")
    .withFallback(ConfigFactory.load())
    .resolve()
  try

    if (config.getString("configfile") != "test.conf")
      throw new IllegalStateException("Must be using test.conf")
  catch {
    case e: Exception =>
      e.printStackTrace()
  }

  def cleanDirectory(): Unit = {
    val datadir = config.get[File]("vizRc210.dataDir").value
    if (Files.exists(datadir.toPath))
      FileUtils.cleanDirectory(datadir)
  }
}
