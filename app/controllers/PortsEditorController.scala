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

import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.KeyFactory.PortKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.mvc._

import javax.inject.Inject

class PortsEditorController @Inject()(implicit val controllerComponents: ControllerComponents, mappedValues: MappedValues,
                                      namedManager: NamedManager) extends BaseController {

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val kv: Map[String, String] = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }

    Ok("todo")
  }

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val portFields: Seq[FieldEntry] = mappedValues(KeyKind.portKey)
      val map: Map[FieldKey, FieldEntry] = portFields
        .map(fieldEntry => fieldEntry.fieldKey -> fieldEntry).
        toMap
      val fieldNames: Seq[String] = portFields.foldLeft(Set.empty[String]) { case (set: Set[String], fieldEntry) =>
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
      } yield
        namedManager.get(portKey) match {
          case Some(value) =>
            Cell(value)
              .withToolTip(s"Port ${portKey.number}")

          case None => Cell(portKey.toString)
        }
      val header = Header("Ports", "Field" +: colHeaders: _*)
      val table = Table(header, rows)

      Ok(views.html.ports(table))
  }

}