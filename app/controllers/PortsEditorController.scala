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

import com.wa9nnn.util.tableui.{Cell, Row, Table}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.key.KeyFactory.PortKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter._
import net.wa9nnn.rc210.ui.FormParser
import play.api.mvc._
import net.wa9nnn.rc210.security.authorzation.AuthFilter.h2u
import javax.inject.Inject

class PortsEditorController @Inject()(implicit val controllerComponents: ControllerComponents,
                                      dataStore: DataStore) extends BaseController {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val portEntries: Seq[FieldEntry] = dataStore(KeyKind.portKey)
      val map: Map[FieldKey, FieldEntry] = portEntries
        .map(fieldEntry => fieldEntry.fieldKey -> fieldEntry).
        toMap
      val fieldNames: Seq[String] = portEntries.foldLeft(Set.empty[String]) { case (set: Set[String], fieldEntry) =>
        set + fieldEntry.fieldKey.fieldName
      }.toSeq
        .sorted

      val rows: Seq[Row] = for {
        fieldName <- fieldNames
      } yield {
        val cells: Seq[Cell] = for {
          number <- 1 to KeyKind.portKey.maxN()
        } yield {
          map(FieldKey(fieldName, KeyFactory(KeyKind.portKey, number))).toCell
        }

        Row(fieldName, cells: _*)
      }

      val colHeaders: Seq[Cell] = for {
        portKey <- KeyFactory[PortKey](KeyKind.portKey)
      } yield {
        portKey.namedCell()
      }
      val namesRow = Row(colHeaders.prepended(Cell("Ports:").withCssClass("cornerCell")))

      val table = Table(Seq.empty, rows.prepended(namesRow))

      Ok(views.html.ports(table))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val updateData = FormParser(AnyContentAsFormUrlEncoded(request.body.asFormUrlEncoded.get))
      dataStore.update(updateData)(h2u(request))
      Redirect(routes.PortsEditorController.index())
  }
}
