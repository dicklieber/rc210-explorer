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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{SelectEnumerationHelper, SelectKeyHelper}
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.data.schedules.{DayOfWeek, MonthOfYear, Schedule}
import play.api.mvc._
import net.wa9nnn.rc210.data.field.FormHelpers._

import javax.inject.{Inject, Singleton}

@Singleton()
class ScheduleController @Inject()(val controllerComponents: ControllerComponents,
                                   mappedValues: MappedValues
                                  )(implicit namedManager: NamedManager) extends BaseController {


  def save(): Action[AnyContent] = Action { implicit request =>
    implicit val valuesMap = request.body.asFormUrlEncoded.get

  val schedule =   Schedule(key = form2Key("key"),
      dayOfWeek = SelectEnumerationHelper(DayOfWeek, "dayOfWeek"),
      weekInMonth = form2OptInt("weekInMonth"),
      monthOfYear = SelectEnumerationHelper(MonthOfYear, "monthOfYear"),
      localTime = form2OptTime("localTime"),
      macroToRun = SelectKeyHelper("macroToRun"))

    val fieldKey = FieldKey("Schedule", schedule.key)
    mappedValues.apply(fieldKey, schedule)

    val sKeyKind = schedule.key.kind.prettyName
    Redirect(routes.EditorController.edit(sKeyKind, schedule.key.toString))
  }
}