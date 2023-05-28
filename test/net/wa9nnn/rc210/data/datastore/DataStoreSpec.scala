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

import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.message.{Message, MesssageExtractor}
import net.wa9nnn.rc210.fixtures.WithTestConfiguration
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.security.authentication.User
import org.specs2.mutable.Specification

import java.nio.file.Files

class DataStoreSpec extends WithTestConfiguration {
  implicit val user = new User(callsign = "W1AW", hash = "1234")

  "DataStore" should {
    "save" in {
      val datFile = new DatFile(config)
      val dataStoreJson = new DataStorePersistence(datFile)
      val dataStore = new DataStore(dataStoreJson)
      val message = Message(KeyFactory.messageKey(1), Seq(1, 2, 3))
      val fieldEntry: FieldEntry = FieldEntry(MesssageExtractor, message)

      Files.exists(datFile.dataStorePath) must beFalse
      dataStore.update(Seq(fieldEntry))
      Files.exists(datFile.dataStorePath) must beTrue
    }
  }
}
