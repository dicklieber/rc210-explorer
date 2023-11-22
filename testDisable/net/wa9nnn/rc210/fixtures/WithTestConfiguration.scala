package net.wa9nnn.rc210.fixtures

import com.typesafe.config.{Config, ConfigFactory}
import net.wa9nnn.RcSpec
import org.apache.commons.io.FileUtils
import net.wa9nnn.rc210.util.Configs

import java.io.File
import java.nio.file.Files

class WithTestConfiguration extends RcSpec {

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
