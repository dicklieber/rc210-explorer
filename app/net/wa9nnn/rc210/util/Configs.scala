/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wa9nnn.rc210.util

import com.github.andyglow.config
import com.github.andyglow.config.FromConf
import com.typesafe.config.Config
import os.*
import java.nio.file.{Paths, Path as JavaPath}

object Configs:
  def path(configPath: String)(using config: Config): JavaPath = {
    val str = config.getString(configPath)
    Paths.get(str)
  }

  implicit val fileOsPath: FromConf[os.Path] = new FromConf[Path] {
    def apply(config: Config, path: String): Path =
      val str = config.getString(path)
      val path1: Path = os.Path(str)
      path1
  }

  