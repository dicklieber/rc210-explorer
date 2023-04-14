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
import net.wa9nnn.rc210.data.datastore.{DataStore, NewCandidate}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.data.timers.Timer
import net.wa9nnn.rc210.data.{FieldKey, datastore}
import net.wa9nnn.rc210.key.KeyFactory.TimerKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.mvc._

import javax.inject._

class TimerEditorController @Inject()(val controllerComponents: ControllerComponents,
                                      mappedValues: DataStore
                                     )(implicit namedManager: NamedManager)
  extends BaseController with LazyLogging {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val timerFields: Seq[FieldEntry] = mappedValues(KeyKind.timerKey)

      val rows: Seq[Row] = timerFields.map { fieldEntry =>
        val value: Timer = fieldEntry.value
        value.toRow()
      }
      val header = Header(s"Timers (${rows.length} values)", "Field", "Seconds", "Macro")
      val table = Table(header, rows)

      Ok(views.html.timers(table))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val kv: Map[String, String] = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }.filterNot(_._1 == "save")
//todo handle name
      mappedValues(kv.map { case (name, formValue) =>
        val fieldKey = FieldKey.fromParam(name)
        datastore.NewCandidate(fieldKey, formValue)
      })

      Redirect(routes.LogicAlarmEditorController.index())
  }
}