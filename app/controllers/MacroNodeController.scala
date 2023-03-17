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

import net.wa9nnn.rc210.data.field.{FieldContents, FieldEntry}
import net.wa9nnn.rc210.data.{Dtmf, FieldKey}
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.named.{NamedManager, NamedSource}
import net.wa9nnn.rc210.key.{FunctionKey, KeyKindEnum, MacroKey}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views.html.macroNodes

import javax.inject.Inject
import play.api.i18n._

import javax.inject.{Inject, Singleton}

@Singleton()
class MacroNodeController @Inject()(val mcc: MessagesControllerComponents,
                                    mappedValues: MappedValues
                                   )(implicit namedSource: NamedManager, functionsProvider: FunctionsProvider) extends MessagesAbstractController(mcc) {

  import net.wa9nnn.rc210.data.field.Formatters._

  val macroForm: Form[MacroNode] = Form(
    mapping(
      "key" -> of[MacroKey],
      "functions" -> seq(of[FunctionKey]),
      "dtmf" -> optional(of[Dtmf])
    )(MacroNode.apply)(MacroNode.unapply)
  )
  val editForm: Form[MacroEdit] = Form(
    mapping(
      "schedule" -> macroForm.mapping,
      "name" -> text
    )(MacroEdit.apply)(MacroEdit.unapply)
  )

  def index(): Action[AnyContent] = Action { implicit request =>

    val value: Seq[MacroNode] = mappedValues.apply(KeyKindEnum.macroKey).map { fieldEntry =>
      fieldEntry.fieldValue.asInstanceOf[MacroNode]
    }
    Ok(macroNodes(value, KeyKindEnum.macroKey))
  }

  def edit(key: MacroKey): Action[AnyContent] = Action { implicit request =>

    val fieldKey = FieldKey("Schedule", key)
    val maybeEntry: Option[FieldEntry] = mappedValues(fieldKey)

    val macroNode: MacroNode = maybeEntry.get.fieldValue.asInstanceOf[MacroNode]

//    val macroNode = MacroNode(key, Seq.empty, Option(Dtmf("12A")))
    val name = namedSource(key)

    val filledInForm = editForm.fill(MacroEdit(macroNode, name))
    val field = filledInForm("scheduke.functions")


    Ok(views.html.macroEditor(filledInForm))
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    implicit val valuesMap = request.body.asFormUrlEncoded.get

    val sKeyKind = KeyKindEnum.macroKey.toString()
    Redirect(routes.EditorController.edit(sKeyKind, MacroKey(1).toString))
  }
}

case class MacroEdit(macroNode: MacroNode, name: String)
