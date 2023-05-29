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

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, ForFieldKey}
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.message.Message
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.key.KeyFactory._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.matching.Regex

@Singleton()
class MessageController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                 (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index(): Action[AnyContent] = Action.async { implicit request =>
    actor.ask[Seq[FieldEntry]](AllForKeyKind(KeyKind.messageKey, _)).map { f: Seq[FieldEntry] =>
      val rows = f.map(_.toRow())
      val table = Table(Message.header(rows.length), rows)
      Ok(views.html.messages(table))
    }
  }

  def edit(key: MessageKey): Action[AnyContent] = Action.async { implicit request =>

    val fieldKey = FieldKey("Message", key)

    actor.ask(ForFieldKey(fieldKey, _)).map {
      case Some(fieldEntry: FieldEntry) =>
        val message: Message = fieldEntry.value
        Ok(views.html.messageEditor(message))
      case None =>
        NotFound(s"Not messageKey: $fieldKey")
    }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    val kv: Map[String, String] = AnyContentAsFormUrlEncoded(request.body.asFormUrlEncoded.get)
      .data
      .map(t => t._1 -> t._2.headOption.getOrElse(""))

    val messageKey: MessageKey = KeyFactory(kv("key"))
    val message = Message(messageKey, kv)
    val candidate = UpdateCandidate(message)
    val name: String = kv("name")
    val namedKey = NamedKey(messageKey, name)

    actor.ask[String](DataStoreActor.UpdateData(Seq(candidate), Seq(namedKey), user=who(request), _)).map{_ =>
      Redirect(routes.MessageController.index())
    }
  }
}

object MessageController {
  val r: Regex = """[^\d]*(\d*)""".r
}

