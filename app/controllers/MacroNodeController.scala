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

import net.wa9nnn.rc210.data.datastore.{DataStore, UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.data.{Dtmf, FieldKey}
import net.wa9nnn.rc210.key.KeyFactory._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.h2u
import play.api.mvc._
import views.html.macroNodes

import javax.inject.{Inject, Singleton}
import scala.util.Try
import scala.util.matching.Regex

@Singleton()
class MacroNodeController @Inject()(dataStore: DataStore
                                   )(implicit  functionsProvider: FunctionsProvider) extends MessagesInjectedController {


  def index(): Action[AnyContent] = Action { implicit request =>

    val value: Seq[MacroNode] = dataStore.apply(KeyKind.macroKey).map { fieldEntry =>
      fieldEntry.value.asInstanceOf[MacroNode]
    }
    Ok(macroNodes(value))
  }

  def edit(key: MacroKey): Action[AnyContent] = Action { implicit request =>

    val fieldKey = FieldKey("Macro", key)
    val maybeEntry: Option[FieldEntry] = dataStore(fieldKey)
    maybeEntry match {
      case Some(fieldEntry: FieldEntry) =>
        Ok(views.html.macroEditor(fieldEntry.value))
      case None =>
        NotFound(s"No keyField: $fieldKey")
    }
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    val formData: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

    val sKey = formData("key").head
    val key: MacroKey = KeyFactory(sKey)
    val dtmf: Option[Dtmf] = formData("dtmf").map(Dtmf(_)).headOption

    val functions: Seq[FunctionKey] = formData("functionIds")
      .head
      .split(",").toIndexedSeq
      .flatMap { sfunction =>
        Try {
          val f: FunctionKey = KeyFactory(sfunction)
          f
        }.toOption
      }

    val macroNode = MacroNode(key, functions, dtmf)
    val ud = UpdateCandidate( macroNode.fieldKey, Right(macroNode))

    val keyNames = Seq(NamedKey(key, formData("name").head))
    dataStore.update(UpdateData(Seq(ud), keyNames))(h2u(request))

    Redirect(routes.MacroNodeController.index())
  }
}

object MacroNodeController {
  val r: Regex = """[^\d]*(\d*)""".r
}


