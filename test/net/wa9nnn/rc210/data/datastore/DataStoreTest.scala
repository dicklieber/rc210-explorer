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

import net.wa9nnn.rc210.data.TriggerNode
import net.wa9nnn.rc210.{FieldKey, Key, WithTestConfiguration}
import net.wa9nnn.rc210.data.field.{FieldDefinitions, FieldEntry}

class DataStoreTest extends WithTestConfiguration {
  private val definitions: FieldDefinitions = new FieldDefinitions
  private val memoryFileLoader: MemoryFileLoader = new MemoryFileLoader(definitions)
  private val dataStorePersistence: DataStorePersistence = new DataStorePersistence()
  val dataStore = new DataStore(dataStorePersistence, memoryFileLoader)
  "DataStore" should {
    "initial" in {
      val all: Seq[FieldEntry] = dataStore.all
      all.length mustBe 341
    }
  }

  "FlowData" should {
    "dump triggers" in {
      val triggers: Seq[FieldEntry] = dataStore.triggerNodes(Key.macroKeys(2))
      triggers.foreach(fe => println(fe))

    }
  }
  "happy path" in {
    val key1 = Key.macroKeys(2)
    val maybeFlowData: Option[FlowData] = dataStore.flow(key1)

    maybeFlowData.foreach{fd =>
      fd.rcMacro.key mustBe key1
      fd.searched mustBe key1
      
      fd.triggers must have length(2)
      fd.triggers.head.toString mustBe "ScheduleNode(Schedule1,EveryDay,Every,Every,23,0,RcMacro3,true)"

      val table = fd.table()
      table
      
      
    }
  }
}