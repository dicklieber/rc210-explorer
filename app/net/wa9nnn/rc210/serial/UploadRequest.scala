/*
 * Copyright (C) 2024  Dick Lieber, WA9NNN                               
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

package net.wa9nnn.rc210.serial

import com.wa9nnn.wa9nnnutil.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import play.api.libs.json.{Format, Json}
import play.api.mvc.PathBindable

import scala.util.matching.Regex

case class UploadRequest(sendField: SendField, fieldKey: Option[FieldKey] = None, acceptCandidate: Boolean = true):
  override def toString: String =
    s"$sendField~${fieldKey.map(_.toString).getOrElse("")}"

  def filter(dataStore: DataStore): Seq[UploadData] =
    fieldKey match
      case Some(fieldKey) =>
        val fieldEntry: FieldEntry = dataStore(fieldKey)
        Seq(UploadData(fieldEntry, fieldEntry.value))
      case None =>
        dataStore
          .all
          .flatMap { fieldEntry =>
            sendField.select(fieldEntry)
          }
  
  def table: Table =
    Table(Header("Upload Request"),
      Seq(
        Row("Send Field", sendField),
        Row("FieldKey", fieldKey)
      )
    )

/**
 * One selected value from the [[net.wa9nnn.rc210.data.datastore.DataStore]]
 *
 * @param fieldEntry
 * @param fieldValue candiate ot fieldValue
 */
case class UploadData(fieldEntry: FieldEntry, fieldValue: FieldValue)

object UploadRequest:
  def apply(sendField: SendField, fieldKey: FieldKey): UploadRequest =
    UploadRequest(sendField, Option(fieldKey))

  private val r: Regex = """(\S+)~(\S+)?""".r

  def apply(s: String): UploadRequest = {
    s match
      case r(sf, fk) =>
        val sendField = SendField.withName(sf)
        val maybeFieldKey: Option[FieldKey] = Option(fk).map(FieldKey.fromId)
        UploadRequest(sendField, maybeFieldKey)
      case x =>
        throw new IllegalArgumentException(s"Can't parse $x")
  }

  implicit val fmtCommandSendRequest: Format[UploadRequest] = Json.format[UploadRequest]

  implicit def pathBinder: PathBindable[UploadRequest] = new PathBindable[UploadRequest] {
    override def bind(key: String, value: String): Either[String, UploadRequest] =

      try {
        Right(UploadRequest(value))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, commandSendRequest: UploadRequest): String =
      commandSendRequest.toString
  }
