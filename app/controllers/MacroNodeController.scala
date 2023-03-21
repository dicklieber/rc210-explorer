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

import net.wa9nnn.rc210.data.{Dtmf, FieldKey}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.{FunctionKey, KeyFormats, KeyKindEnum, MacroKey}
import play.api.libs.Files
import play.api.mvc._
import views.html.macroNodes

import java.nio.file.Files
import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton()
class MacroNodeController @Inject()(val mcc: MessagesControllerComponents,
                                    mappedValues: MappedValues
                                   )(implicit namedSource: NamedManager, functionsProvider: FunctionsProvider) extends MessagesAbstractController(mcc) {


  def index(): Action[AnyContent] = Action { implicit request =>

    val value: Seq[MacroNode] = mappedValues.apply(KeyKindEnum.macroKey).map { fieldEntry =>
      fieldEntry.fieldValue.asInstanceOf[MacroNode]
    }
    Ok(macroNodes(value, KeyKindEnum.macroKey))
  }

  def edit(key: MacroKey): Action[AnyContent] = Action { implicit request =>

    val maybeEntry: Seq[FieldEntry] = mappedValues(key)
    val macroNode: MacroNode = maybeEntry.head.fieldValue.asInstanceOf[MacroNode]
    val name = namedSource(key)


    Ok(views.html.macroEditor(macroNode, name))
  }

  import MacroNodeController.r

  def save(): Action[AnyContent] = Action { implicit request =>
    val multipartFormData = request.body.asMultipartFormData
    implicit val valuesMap = multipartFormData.get

    val dataParts: Map[String, Seq[String]] = valuesMap.dataParts
    val sKey = dataParts("key").head
    val key:MacroKey = KeyFormats(sKey)
    val dtmf: Option[Dtmf] = dataParts("dtmf").map(Dtmf(_)).headOption

    val functions: Seq[FunctionKey] = dataParts("functionIds")
      .head
      .split(",").toIndexedSeq
      .flatMap {sfunction =>
        Try{
          val r(n) = sfunction
          FunctionKey(n.toInt)
        }.toOption
      }


    val sKeyKind = KeyKindEnum.macroKey.toString()

    val newMacroNode = MacroNode(key, functions, dtmf)

    mappedValues(newMacroNode.fieldkey, newMacroNode)
    Redirect(routes.EditorController.edit(sKeyKind, key.toString))
  }
}

object MacroNodeController {
  val r = """[^\d]*(\d*)""".r
}

case class MacroEdit(macroNode: MacroNode, name: String)
