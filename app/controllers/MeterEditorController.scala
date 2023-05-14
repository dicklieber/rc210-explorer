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
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.Formatters._
import net.wa9nnn.rc210.data.meter._
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, MeterAlarmKey, MeterKey}
import net.wa9nnn.rc210.key.KeyFormats._
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.mvc._

import javax.inject._

class MeterEditorController @Inject()(dataStore: DataStore) extends MessagesInjectedController with LazyLogging {


  val alarmMapping: Mapping[MeterAlarm] =
    mapping(
      "key" -> of[MeterAlarmKey],
      "name" -> text,
      "meter" -> of[MeterKey],
      "alarmType" -> of[AlarmType],
      "tripPoint" -> default(number, 0  ),
      "macroKey" -> of[MacroKey]
    )(MeterAlarm.apply)(MeterAlarm.unapply)


  val voltToReadingMapping: Mapping[VoltToReading] =
    mapping(
      "hundredthVolt" -> number,
      "reading" -> number,
    )(VoltToReading.apply)(VoltToReading.unapply)

  val meterMapping: Mapping[Meter] =
    mapping(
      "key" -> of[MeterKey],
      "name" -> text,
      "faceName" -> of[MeterFaceName],
      "low" -> voltToReadingMapping,
      "high" -> voltToReadingMapping,
    )(Meter.apply)(Meter.unapply)



  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
//      val fieldKey = Meters.fieldKey(KeyFactory.meterKey())
//      val fieldEntry: FieldEntry = dataStore(fieldKey).get
//      val meters: Meters = fieldEntry.value.asInstanceOf[Meters]
//
//      val filledInForm = metersForm.fill(meters)
//
//      Ok(views.html.meters(filledInForm))
      Ok("todo")
  }

  def save(): Action[AnyContent] = Action {
    implicit request =>

//
//      metersForm.bindFromRequest.fold(
//        formWithErrors => {
//          BadRequest(views.html.meters(formWithErrors))
//        },
//        meters => {
//          val updateCandidate = UpdateCandidate(meters)
//          dataStore.update(UpdateData(Seq(updateCandidate)))
          Redirect(routes.MeterEditorController.index)
//        }
//      )
  }
}