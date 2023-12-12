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
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.datastore.{DataStore, DataStoreActor}
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.libs.Files
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class DataStoreController @Inject()(dataStore: DataStore)
                                   (implicit scheduler: Scheduler, ec: ExecutionContext, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds


  def downloadJson(): Action[AnyContent] = Action.async {
    actor.ask(DataStoreActor.Json.apply).map { sJson =>
      Ok(sJson).withHeaders(
        "Content-Type" -> "text/json",
        "Content-Disposition" -> s"""attachment; filename="rc210.json""""
      )
    }
  }

  def viewJson(): Action[AnyContent] = Action.async {
    actor.ask(DataStoreActor.Json.apply).map { sJson =>
      Ok(sJson)
    }
  }

  def upload: Action[AnyContent] = Action {
    Ok(views.html.fileUpload())
  }

  def save: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    val body = request.body
    body
      .file("jsonFile")
      .foreach { jsonFile =>
        val sJson = java.nio.file.Files.readString(jsonFile.ref.path)
        actor.ask(IngestJson(sJson, _))
      }
    Redirect(routes.MacroEditorController.index())
  }

}