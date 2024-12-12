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

/**
 *
 * @param doCandidateOnly        true to upload the candidate. False to upload the fieldValue
 * @param acceptCandidate        true to set the fieldValue to the candidate, if present. Clears the candidate.
 * @param maybeFieldKey          if Some then just upload this field. None uploads all fields as conditioned by [[doCandidateOnly]].
 */
case class UploadRequest(doCandidateOnly: Boolean = true,
                         acceptCandidate: Boolean = true,
                         maybeFieldKey: Option[FieldKey] = None):
  override def toString: String =
    val prefix: String = maybeFieldKey match
      case Some(fieldKey: FieldKey) =>
        s"Send ${fieldKey.display}"
      case None =>
        if(doCandidateOnly)
          "Send all candidate fields"
        else
          "Send all values; candidate or current field value"
    if(acceptCandidate)
      prefix + " and accept candidate."
    else
      prefix
    

  def filter(dataStore: DataStore): Seq[UploadData] =
    maybeFieldKey match
      case Some(fieldKey) =>
        val fieldEntry: FieldEntry = dataStore.fieldEntry(fieldKey)
        Seq(UploadData(fieldEntry, fieldEntry.value, acceptCandidate))
      case None =>
        for {
          fieldEntry <- dataStore.all
          if (fieldEntry.hasCandidate || !doCandidateOnly)
        } yield {
          UploadData(
            fieldEntry = fieldEntry,
            fieldValue = if (doCandidateOnly)
              fieldEntry.value
            else
              fieldEntry.fieldData.fieldValue,
            accept = acceptCandidate
          )
        }
  
  def table: Table =
    Table(Header("Upload Request"),
      Seq(
        Row("Upload", if (doCandidateOnly) "Candidate" else "Field Value"),
        Row("Accept", if (acceptCandidate) "Candidate to Field Value" else "Keep Candidate"),
        Row("FieldKey", maybeFieldKey match
          case Some(fieldKey) =>
            fieldKey.display
          case None =>
            "All fields"
        )
      )
    )

/**
 * One selected value from the [[net.wa9nnn.rc210.data.datastore.DataStore]]
 *
 * @param fieldEntry
 * @param fieldValue candiate ot fieldValue
 * @param accept     true to make fieldValue to candidate, clearing existing candidate.
 */
case class UploadData(fieldEntry: FieldEntry, fieldValue: FieldValue, accept: Boolean)

object UploadRequest:
  implicit val fmtUploadRequest: Format[UploadRequest] = Json.format[UploadRequest]

    def apply(fieldKey: FieldKey): UploadRequest =
      UploadRequest(maybeFieldKey =  Option(fieldKey))


  implicit def pathBinder: PathBindable[UploadRequest] = new PathBindable[UploadRequest] {
    override def bind(key: String, value: String): Either[String, UploadRequest] =

      try {
        Right(Json.parse(value).as[UploadRequest])
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, uploadRequest: UploadRequest): String = {
      val jsValue = Json.toJson(uploadRequest)
      jsValue.toString
    }
  }
