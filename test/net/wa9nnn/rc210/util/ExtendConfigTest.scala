package net.wa9nnn.rc210.util

import net.wa9nnn.rc210.WithTestConfiguration
import net.wa9nnn.rc210.util.Configs.*
import com.github.andyglow.config.*


class ExtendConfigTest extends WithTestConfiguration {
  "ExtendConfig" when {
    "JavaPath" in {
      val value1: os.Path = config.get[os.Path]("vizRc210.memoryFile")
      os.exists(value1) mustEqual(true)
    }
  }
}
