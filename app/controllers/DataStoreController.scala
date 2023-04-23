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

package controllers

import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.{DataStore, DataStoreJson}
import net.wa9nnn.rc210.data.field.FieldValue
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt

@Singleton
class DataStoreController @Inject()(implicit val controllerComponents: ControllerComponents,
                                    dataStore: DataStore,
                                    dataStoreJson: DataStoreJson
                                   ) extends BaseController with LazyLogging {
  implicit val timeout: Timeout = 5.seconds

  def downloadJson(): Action[AnyContent] = Action {

    val sJson = Json.prettyPrint(Json.toJson(dataStore.toJson))

    Ok(sJson).withHeaders(
      "Content-Type" -> "text/json",
      "Content-Disposition" -> s"""attachment; filename="rc210.json""""
    )
  }

  def upload: Action[AnyContent] = Action {
    Ok(views.html.fileUpload())
  }

  def save: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    val body = request.body
    body
      .file("jsonFile")
      .foreach { jsonFile: MultipartFormData.FilePart[Files.TemporaryFile] =>
        dataStoreJson.load(dataStore, jsonFile.ref.path)
      }
    Redirect(routes.MacroNodeController.index())
  }

    def dump(): Action[AnyContent] = Action {

      val rows: Seq[Row] = dataStore
        .all
        .map { fieldEntry =>
          val value: FieldValue = fieldEntry.value
          val fieldKey = fieldEntry.fieldKey
          val key = fieldKey.key
          var row = Row(key.toString, key.keyName, fieldKey.fieldName, value.display, value.toCommand(fieldEntry))

          fieldEntry.candidate.foreach { candidateValue =>
            row = row :+ candidateValue.display
            row = row :+ candidateValue.toCommand(fieldEntry)
          }
          row
        }
      val header = Header(s"All entries (${rows.length})", "Key","KeyName", "Field Name",  "Field Value", "Command", "Candidate Value", "Candidate Command")
      val table = Table(header, rows)
      Ok(views.html.dat(Seq(table)))
    }
}