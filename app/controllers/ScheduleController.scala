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
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.mvc._

import javax.inject.{Inject, Singleton}

@Singleton()
class ScheduleController @Inject()(val controllerComponents: ControllerComponents,
                                   mappedValues: MappedValues
                                  )(implicit namedManager: NamedManager) extends BaseController {


  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val entries: Seq[FieldEntry] = mappedValues(KeyKind.scheduleKey)
      val rows = entries.map { fieldEntry: FieldEntry =>
        val schedule: Schedule = fieldEntry.fieldValue.asInstanceOf[Schedule]
        val dow: Cell = Cell.rawHtml(schedule.dayOfWeek.toHtmlField(fieldEntry))
        val weekInMonth: Cell = Cell.rawHtml(s"""<input type="range" name="Week" min="0" max="5">""")
        val woy: Cell = Cell.rawHtml(schedule.monthOfYear.toHtmlField(fieldEntry))
        val localTime: Cell = Cell.rawHtml((s"""<input type="time" name="Time" value="${schedule.localTime}">"""))
        val macroToRun: Cell = schedule.macroToRun.toCell //todo need a macroselect control.

        Row(Seq(
          schedule.key.toCell,
          dow,
          weekInMonth,
          woy,
          localTime,
          macroToRun
        ))
      }
      val columnHeaders: Seq[Cell] = for {
        portKey <- KeyFactory(KeyKind.portKey)
      } yield
        namedManager.get(portKey) match {
          case Some(value) =>
            Cell(value)
              .withToolTip(s"Port ${portKey.number}")

          case None => Cell(portKey.toString)
        }

      val table = Table(Header("Schedules", columnHeaders: _*), rows)
      Ok(views.html.schedules(table))
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    implicit val valuesMap = request.body.asFormUrlEncoded.get

    //    val schedule = Schedule(key = form2Key("key"),
    //      dayOfWeek = SelectField(ScheduleEnums.dayOfWeek, "dayOfWeek"),
    //      weekInMonth = form2OptInt("weekInMonth"),
    //      monthOfYear = SelectEnumerationHelper(MonthOfYear, "monthOfYear"),
    //      localTime = form2OptTime("localTime"),
    //      macroToRun = SelectKeyHelper("macroToRun"))
    //
    //    val fieldKey = FieldKey("Schedule", schedule.key)
    //    mappedValues.apply(fieldKey, schedule)
    Ok("todo after edit save schedule")
    //    Redirect(routes.EditorController.edit(KeyKind.scheduleKey, schedule.key.toString))
  }
}
