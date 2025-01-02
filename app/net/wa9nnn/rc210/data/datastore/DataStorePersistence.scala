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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210explorer.BuildInfo
import play.api.libs.json.*

import java.nio.file.{Files, Path}
import java.time.Instant

/**
 * Adds persistence to the [[DataStoreEngine]].
 */
class DataStorePersistence() extends DataStoreEngine with LazyLogging:
  private var metadata: Metadata = Metadata.empty

  def loadFile(path: Path): Unit = 
    val sSkip = System.getProperty("skipLoadJson", "false")
    if Files.exists(path) && sSkip != "true" then
      fromJson(Files.readString(path))

  def saveFile(path: Path, rcSession: RcSession): Unit =
    metadata = Metadata(
      BuildInfo.name + "-data",
      BuildInfo.version,
      Instant.now().toString,
      rcSession.user.callsign
    )
    val sJson = toJson()
    Files.writeString(path, sJson)

  def toJson(): String =
    val values: Seq[JsObject] = entries.map { fieldEntry =>
      val fieldData = fieldEntry.fieldData

      val f: Writes[FieldValue] = fieldEntry.fieldDefinition.fmt.asInstanceOf[Writes[FieldValue]]
      val valueJs = f.writes(fieldData.fieldValue)
      val withoutCandidate = Json.obj(
        "key" -> fieldEntry.key.id,
        "current" -> valueJs,
      )
      fieldData.candidate match
        case Some(candidate) =>
          withoutCandidate + ("candidate" -> f.writes(candidate))
        case None =>
          withoutCandidate
    }
    val o = Json.obj(
      "metadata" -> metadata,
      "data" -> values,
    )

    Json.prettyPrint(o)

  def fromJson(sJson: String): Unit =
    val jsValue = Json.parse(sJson)
    val jsArray: JsArray = jsValue.as[JsArray]
    jsArray.value.foreach { (obj: JsValue) =>
      val value: JsLookupResult = obj \ "key"
      val id = value.as[String]
      val key: Key = Key.fromId(id)
      val fe: FieldEntry = getFieldEntry(key)
      //      val fieldData: FieldData = obj.as[FieldData]
      //      fe.set(fieldData)
    }

case class Metadata(title: String, version: String, stamp: String, user: String)

object Metadata:
  implicit val fmt: Format[Metadata] = Json.format[Metadata]
  val empty: Metadata = Metadata("", "", "", "")