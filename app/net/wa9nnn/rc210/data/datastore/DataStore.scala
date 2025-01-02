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

package net.wa9nnn.rc210.data.datastore

import com.fasterxml.jackson.module.scala.deser.overrides
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210
import net.wa9nnn.rc210.*
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.ui.NamedKeyManager
import net.wa9nnn.rc210.util.Configs

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

/**
 * This is the in-memory source of all RC-210 and NamedKey data.
 */
@Singleton
class DataStore @Inject()(config: Config,
                          memoryFileLoader: MemoryFileLoader) extends DataStorePersistence with LazyLogging:
  private val path: Path = Configs.path("vizRc210.dataStoreFile")(using config)

  reload() // initial

  def reload(): Unit =
    loadDefinitions(memoryFileLoader.extractFieldEntries())
    loadJsonFile(path)

  def update(candidates: Seq[UpdateCandidate], rcSession: RcSession): Unit =
    super.update(candidates)
    saveFile(path, rcSession)

