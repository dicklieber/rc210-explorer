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
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.key.KeyFactory.ScheduleKey
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.util.MacroSelect
import play.api.mvc._
import net.wa9nnn.rc210.data.schedules.Schedule.apply
import javax.inject.{Inject, Singleton}

@Singleton()
class ScheduleController @Inject()(val controllerComponents: ControllerComponents,
                                   mappedValues: MappedValues
                                  )(implicit namedSource: NamedManager) extends BaseController with LazyLogging {


  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val entries: Seq[FieldEntry] = mappedValues(KeyKind.scheduleKey)
      val rows: Seq[Row] = entries.map { fieldEntry: FieldEntry =>
        val schedule: Schedule = fieldEntry.value
        schedule.toRow()
      }

      val table = Table(Header("Schedules",
        "SetPoint",
        "Enabled",
        "Day in Week",
        Cell("Week").withToolTip("Week in month. 0 disables"),
        "Month",
        "Time",
        "Macro To Run"),
        rows.take(1))
      Ok(views.html.schedules(table))
  }

  def save(): Action[AnyContent] = Action { implicit request =>
    val valuesMap: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
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
      .map { case (key, items: Seq[(FieldKey, String)]) =>
        logger.trace(s"==== {} ====", key.toString)

        implicit val nameToValue: Map[String, String] = items.map { case (fieldey, value) =>
          fieldey.fieldName -> value
        }.toMap

        Schedule((key.asInstanceOf[ScheduleKey]))
      }.toSeq.sortBy(_.key)

    mappedValues.apply(r)


    Redirect(routes.ScheduleController.index())
  }
}
