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
import net.wa9nnn.rc210.{FieldKey, KeyKind, NamedKey}
import net.wa9nnn.rc210.data.clock.ClockNode.{form, keyKind}
import net.wa9nnn.rc210.data.clock.{ClockNode, DSTPoint}
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldValue}
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.ProcessResult
import play.api.data.Form
import play.api.i18n.MessagesProvider
import play.api.mvc.*
import play.twirl.api.Html
import views.html.fieldIndex

import java.util.Optional
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

@Singleton()
class EditController @Inject()(implicit dataStore: DataStore, ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components)
    with LazyLogging {

  def index(keyKind: KeyKind): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] => {
      keyKind.handler.redirect(keyKind) match
        case Some(call) =>
          Redirect(call)

        case None =>
          val value = dataStore.indexValues(keyKind)
          val table: Table = keyKind.handler.index(value)
          val html: Html = fieldIndex(keyKind, table)
          Ok(html)
    }
  }

  def edit(fieldKey: FieldKey): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] => {
      val entry: FieldEntry = dataStore.apply(fieldKey)
      val editHandler = fieldKey.editHandler
      editHandler.edit(entry)
    }
  }

  def save(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>

      given data: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

      given RcSession = request.attrs(sessionKey)

      logger.whenDebugEnabled {
        data.foreach { (name, values) =>
          println(s"$name: ${values.mkString(", ")}")
        }
      }

      val fieldKey: FieldKey = ExtractField("fieldKey", (value) => FieldKey(value))
      val name: String = ExtractField("name", (value) => value)
      val handler: EditHandler[?] = fieldKey.key.keyKind.handler
      val value1: ComplexFieldValue = handler.bindFromRequest(data)
      val updateCandidate = UpdateCandidate(fieldKey, value1)
      val function: Option[NamedKey] = Option.when(name.nonEmpty)(NamedKey(fieldKey.key, name))
      val candidateAndNames: CandidateAndNames = CandidateAndNames(updateCandidate, function)
      dataStore.update(candidateAndNames)
      handler.saveOp()
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


