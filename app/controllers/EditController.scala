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
import com.wa9nnn.wa9nnnutil.tableui.Table
import net.wa9nnn.rc210
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldValue}
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.NavMainController
import net.wa9nnn.rc210.{FieldKey, KeyKind, NamedKey}
import play.api.i18n.MessagesProvider
import play.api.mvc.*
import play.twirl.api.Html
import views.html.{NavMain, fieldIndex}

import javax.inject.{Inject, Singleton}
import scala.collection.immutable
import scala.concurrent.ExecutionContext

@Singleton()
class EditController @Inject()(navMain: NavMain)
                              (implicit dataStore: DataStore,
                               ec: ExecutionContext, components: ControllerComponents)
  extends NavMainController(components) with LazyLogging {

  def index(keyKind: KeyKind): Action[AnyContent] = Action {
    implicit request => {
      val value: Seq[FieldEntry] = dataStore(keyKind)
      val content: Html = keyKind.handler.index(value)
      Ok(navMain(keyKind, content))
    }
  }

  def edit(fieldKey: FieldKey): Action[AnyContent] = Action {
    implicit request =>
      val entry: FieldEntry = dataStore.apply(fieldKey)
      val editHandler = fieldKey.editHandler
      val html: Html = editHandler.edit(entry)
      Ok(navMain(fieldKey.key.keyKind, html))
  }

  def save(): Action[AnyContent] = Action{
    implicit request: Request[AnyContent] =>

      given data: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

      given RcSession = request.attrs(sessionKey)

      logger.whenDebugEnabled {
        data.foreach { (name, values) =>
          println(s"$name: ${values.mkString(", ")}")
        }
      }

      try
        val fieldKey: FieldKey = ExtractField("fieldKey", (value) => FieldKey(value))
        val handler: EditHandler = fieldKey.key.keyKind.handler
        val updateCandidates: Seq[UpdateCandidate] = handler.bindFromRequest(data)
        val namedKeys: Seq[NamedKey] = (for{
          (sfKey: String, values: Seq[String]) <- data
          fieldKey = FieldKey(sfKey)
          if fieldKey.fieldName == "name"
          name <- values.headOption
        }yield{
          NamedKey(fieldKey.key, name)
        }).toSeq

        val candidateAndNames: CandidateAndNames = CandidateAndNames(updateCandidates, namedKeys)
        dataStore.update(candidateAndNames)
        handler.saveOp()
      catch
        case e:Exception =>
          logger.error(s"save: ${e.getMessage}", e)
          InternalServerError(s"request.toString $e.getMessage")
  }
}

object ExtractField:
  def apply[T](name: String, f: (String) => T)(using encoded: Map[String, Seq[String]]): T =
    (for {
      e <- encoded.get(name)
      v <- e.headOption
    } yield {
      f(v)
    }).getOrElse(throw new IllegalArgumentException(s"No $name in body!"))


