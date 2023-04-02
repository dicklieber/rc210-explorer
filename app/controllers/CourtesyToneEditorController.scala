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
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.courtesy.CourtesyTone
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.mapped.{MappedValues, NewCandidate}
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.KeyKind
import play.api.mvc._

import javax.inject._

class CourtesyToneEditorController @Inject()(val controllerComponents: ControllerComponents,
                                             mappedValues: MappedValues
                                            )(implicit namedManager: NamedManager)
  extends BaseController with LazyLogging {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val entries: Seq[FieldEntry] = mappedValues(KeyKind.courtesyToneKey)
      val rows: Seq[Row] = entries.flatMap { fe =>
        val ct: CourtesyTone = fe.value
        ct.rows()
      }

      val header = Header(s"Courtesy Tones (${entries.length} values)",
        "Name",
        Cell("Segment 1").withColSpan(2),
        Cell("Segment 2").withColSpan(2),
        Cell("Segment 3").withColSpan(2),
        Cell("Segment 4").withColSpan(2))
      val table = Table(header, rows)

      Ok(views.html.coourtesyTones(table))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val kv: Map[String, String] = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }.filterNot(_._1 == "save")

      mappedValues(kv.map { case (name, formValue) =>
        val fieldKey = FieldKey.fromParam(name)
        NewCandidate(fieldKey, formValue)
      })

      Redirect(routes.LogicAlarmEditorController.index())
  }
}