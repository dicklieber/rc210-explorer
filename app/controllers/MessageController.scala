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

import com.wa9nnn.util.tableui.{Row, Table}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.{DataStore, UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.message.Message
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.key.KeyFactory._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.ui.FormParser
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.util.matching.Regex

@Singleton()
class MessageController @Inject()(dataStore: DataStore) extends MessagesInjectedController {


  def index(): Action[AnyContent] = Action { implicit request =>

    val rows: Seq[Row] = dataStore.apply(KeyKind.messageKey)
      .map(_.value[Message].toRow)
    val table = Table(Message.header(rows.length), rows)
    Ok(views.html.messages(table))
  }

  def edit(key: MessageKey): Action[AnyContent] = Action { implicit request =>

    val fieldKey = FieldKey("Message", key)
    val maybeEntry: Option[FieldEntry] = dataStore(fieldKey)
    maybeEntry match {
      case Some(fieldEntry: FieldEntry) =>
        val value: Message = fieldEntry.value
        Ok(views.html.messageEditor(value))

      case None =>
        NotFound(s"No keyField: $fieldKey")
    }
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    val kv: Map[String, String] = AnyContentAsFormUrlEncoded(request.body.asFormUrlEncoded.get)
      .data
      .map(t => t._1 -> t._2.headOption.getOrElse(""))

    val messageKey: MessageKey = KeyFactory(kv("key"))
    val message = Message(messageKey, kv)
    val candidate = UpdateCandidate(message)
    val name: String = kv("name")
    val namedKey = NamedKey(messageKey, name)

    dataStore.update(UpdateData(Seq(candidate), Seq(namedKey)))
    Redirect(routes.MessageController.index())
  }
}

object MessageController {
  val r: Regex = """[^\d]*(\d*)""".r
}

