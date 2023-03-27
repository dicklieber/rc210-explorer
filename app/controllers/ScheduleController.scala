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
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.named.{NamedManager, NamedSource}
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.key.KeyKind
import play.api.mvc._

import javax.inject.{Inject, Singleton}

@Singleton()
class ScheduleController @Inject()(val controllerComponents: ControllerComponents,
                                   mappedValues: MappedValues
                                  )(implicit namedSource: NamedManager) extends BaseController with LazyLogging {


  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val entries: Seq[FieldEntry] = mappedValues(KeyKind.scheduleKey)
      val rows: Seq[Row] = entries.map { fieldEntry: FieldEntry =>
        val schedule: Schedule = fieldEntry.fieldValue.asInstanceOf[Schedule]
        val keyName = namedSource.get(schedule.key).getOrElse("")
        val name: Cell = Cell.rawHtml(views.html.fieldNamedKey(schedule.key, keyName, schedule).toString())
        val dow: Cell = Cell.rawHtml(schedule.dayOfWeek.toHtmlField(fieldEntry))
        val weekInMonth: Cell = Cell.rawHtml(s"""<input type="range" name="${FieldKey("Week", schedule.key).param}" min="0" max="5">""")
        val woy: Cell = Cell.rawHtml(schedule.monthOfYear.toHtmlField(fieldEntry))
        val localTime: Cell = Cell.rawHtml(s"""<input type="time" name="${FieldKey("Time", schedule.key).param}" value="${schedule.localTime}">""")
        val macroToRun: Cell = schedule.selectedMacroToRun.toCell(schedule)

        Row(Seq(
          name,
          dow,
          weekInMonth,
          woy,
          localTime,
          macroToRun
        ))
      }

      val table = Table(Header("Schedules",
        "SetPoint",
        "Day in Week",
        Cell("Week").withToolTip("Week in month. 0 disables"),
        "Month",
        "Time",
        "Macro To Run"),
        rows)
      Ok(views.html.schedules(table))
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    implicit val valuesMap: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
    val r = valuesMap.map { case (sKey, values) =>
      sKey -> values.headOption.getOrElse("No value")
    }.filter(_._1 != "save")
      .toSeq
      .map { case (name, value) =>
        val fk = FieldKey.fromParam(name)
        logger.trace("name: {} value: {} fieldKey: {}", name, value, fk)
        fk -> value
      }
      .sortBy(_._1.key.number)
      .groupBy(_._1.key)
      .foreach { case (key, items) =>
        logger.trace(s"==== {} ====", key.toString)
        items.sortBy(_._1.fieldName)
          .foreach { case (fk, value) =>
            logger.trace("fk: {} value: {}", fk.fieldName, value)
          }
      }





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
