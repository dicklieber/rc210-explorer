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

import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import net.wa9nnn.rc210.ui.FormParser
import play.api.mvc._

import javax.inject.Inject

class CommonEditorController @Inject()(implicit val controllerComponents: ControllerComponents, dataStore: DataStore)
  extends BaseController {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val commonFields: Seq[FieldEntry] = dataStore(KeyKind.commonKey)

      val rows: Seq[Row] = commonFields.map { fieldEntry =>
        // Can't use fieldEntry's toRow because we just want the rc2input name not key, as they are all commonKey1
        Row(
          fieldEntry.fieldKey.fieldName,
          fieldEntry.toCell
        )
      }

      val header = Header(s"Common (${rows.length} values)", "Field", "Value")
      val table = Table(header, rows)

      Ok(views.html.common(table))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val updateData = FormParser(AnyContentAsFormUrlEncoded(request.body.asFormUrlEncoded.get))
      dataStore.update(updateData)(who(request))
      Redirect(routes.CommonEditorController.index())
  }
}
