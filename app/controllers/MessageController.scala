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
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.message.Message
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.Try
import scala.util.matching.Regex
import net.wa9nnn.rc210.security.Who.request2Session
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey

@Singleton()
class MessageController @Inject()(dataStore: DataStore, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {

  def index(): Action[AnyContent] = Action { implicit request =>
    val messages: Seq[Message] = dataStore.indexValues(KeyKind.Message)
    Ok(views.html.messages(messages))
  }

  def edit(key: Key): Action[AnyContent] = Action { implicit request =>
    val fieldKey = FieldKey("Message", key)
    val message: Message = dataStore.editValue(fieldKey)
    Ok(views.html.messageEditor(message))
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    val formData: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

    val sKey: String = formData("key").head
    val messageKey: Key = Key.apply(sKey)

    val words: Seq[Int] = formData("ids")
      .head
      .split(",").toIndexedSeq
      .flatMap { sWord =>
        Try {
          sWord.toInt
        }.toOption
      }

    val message = Message(messageKey, words)
    val candidateAndNames = ProcessResult(message)

    given RcSession = request.attrs(sessionKey)

    dataStore.update(candidateAndNames) 
    Redirect(routes.MessageController.index())
  }
}

object MessageController {
  val r: Regex = """[^\d]*(\d*)""".r
}

