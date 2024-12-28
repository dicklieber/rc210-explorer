/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */

package net.wa9nnn.rc210.data.datastore

import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.{Key, KeyMetadata, NamedKey}
import net.wa9nnn.rc210.data.field.{FieldData, FieldEntry, FieldValue}
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.util.Configs
import play.api.libs.json.*

import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.Seq
import scala.util.{Failure, Try, Using}

/**
 * Adds persistence to the [[DataStoreEngine]].
 */
class DataStorePersistence() extends DataStoreEngine with LazyLogging:
  def loadFile(path: Path): Unit = {
    val sSkip = System.getProperty("skipLoadJson", "false")
    if Files.exists(path) && sSkip != "true" then
      fromJson(Files.readString(path))
  }

  def saveFile(path: Path): Unit =
    val sJson = toJson
    Files.writeString(path, sJson)

  def toJson: String =
    val a = Json.arr(
      entries.map { fieldEntry =>
        val fieldData = fieldEntry.fieldData

        implicit val f: Format[?] = fieldEntry.fieldDefinition.fmt 
        val toJson1: JsValue = Json.toJson(fieldData)
        toJson1
      }
    )

    Json.prettyPrint(a)

  def fromJson(sJson: String): Unit =
    val jsValue = Json.parse(sJson)
    val jsArray: JsArray = jsValue.as[JsArray]
    jsArray.value.foreach { (obj: JsValue) =>
      val value: JsLookupResult = obj \ "key"
      val id = value.as[String]
      val key: Key = Key.fromId(id)
      val fe: FieldEntry = getFieldEntry(key)

      val fieldData: FieldData = obj.as[FieldData]
      fe.set(fieldData)
    }



