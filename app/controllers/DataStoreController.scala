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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.datastore.{DataStore, DataTransferJson}
import net.wa9nnn.rc210.ui.nav.TabKind
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.{Action, *}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class DataStoreController @Inject()(dataStore: DataStore)
                                   (implicit cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {

  private def sJson: String =
    val dto: DataTransferJson = dataStore.toJson
    Json.prettyPrint(Json.toJson(dto))

  def downloadJson: Action[AnyContent] = Action {
    Ok(sJson).withHeaders(
      "Content-Type" -> "text/json",
      "Content-Disposition" -> s"""attachment; filename="rc210.json""""
    )
  }

  def viewJson: Action[AnyContent] = Action {
    Ok(sJson)
  }

  def upload(): Action[AnyContent] = Action {
    Ok(views.html.fileUpload())
  }

  def save: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    val body = request.body
    body
      .file("jsonFile")
      .foreach { jsonFile =>
        val sJson = java.nio.file.Files.readString(jsonFile.ref.path)
        val dataTransferJson: DataTransferJson = Json.parse(sJson).as[DataTransferJson]
        dataStore(dataTransferJson)
        
      }
    Redirect(routes.NavigationController.selectTabKind(TabKind.Fields))
  }
  def rollback(): Action[AnyContent] = Action {
    dataStore.rollback()
    Redirect(routes.NavigationController.selectTabKind(TabKind.Fields))
  }
}