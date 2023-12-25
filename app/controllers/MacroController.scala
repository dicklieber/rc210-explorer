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
import net.wa9nnn.rc210.data.Dtmf.Dtmf
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.macros.RcMacro
import net.wa9nnn.rc210.data.macros.RcMacro.*
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.mvc.*
import views.html.macroNodes

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.matching.Regex

@Singleton()
class MacroController @Inject()(dataStore: DataStore)
                                     (implicit
                                      functionsProvider: FunctionsProvider,
                                      ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components)
    with LazyLogging {

  def index: Action[AnyContent] = Action { implicit request =>
    val values: Seq[RcMacro] = dataStore.values(KeyKind.RcMacro)
    Ok(macroNodes(values))
  }

  def edit(key: Key): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>

    val fieldKey = FieldKey( key)
    val rcMacro: RcMacro = dataStore.editValue(fieldKey)
    Ok(views.html.macroEditor(rcMacro))
  }

  def save(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val formData: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

    val sKey: String = formData("key").head
    val key: Key = Key.apply(sKey)
    val dtmf = (for {
      d: Dtmf <- formData("dtmf")
      if d.nonEmpty
    } yield {
      d
    }).headOption
    val functions: Seq[Key] = formData("ids")
      .head
      .split(",").toIndexedSeq
      .flatMap { sfunction =>
        Try {
          val f: Key = Key(sfunction)
          f
        }.toOption
      }

    val rcMacro = RcMacro(key, functions, dtmf)
    val candidateAndNames = ProcessResult(rcMacro)

    given RcSession = request.attrs(AuthFilter.sessionKey)

    dataStore.update(candidateAndNames)

    Redirect(routes.MacroController.index)
  }
}

object MacroController {
  val r: Regex = """[^\d]*(\d*)""".r
}


