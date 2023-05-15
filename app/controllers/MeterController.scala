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
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.{DataStore, UpdateCandidate}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldInt}
import net.wa9nnn.rc210.data.field.Formatters._
import net.wa9nnn.rc210.data.meter._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, MeterAlarmKey, MeterKey, meterAlarmKey}
import net.wa9nnn.rc210.key.KeyFormats._
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc._

import javax.inject._

class MeterController @Inject()(dataStore: DataStore) extends MessagesInjectedController with LazyLogging {

  /*

  val alarmMapping: Mapping[MeterAlarm] =
    mapping(
      "key" -> of[MeterAlarmKey],
      "meter" -> of[MeterKey],
      "alarmType" -> of[AlarmType],
      "tripPoint" -> default(number, 0),
      "macroKey" -> of[MacroKey]
    )((key: MeterAlarmKey, name: String, meter: MeterKey, alarmType: AlarmType, tripPoint: Int, macroKey: MacroKey) => MeterAlarm.apply(key, meter, alarmType, tripPoint, macroKey))((key, name, meter, alarmType, tripPoint, macroKey) => MeterAlarm.unapply())


  val voltToReadingMapping: Mapping[VoltToReading] =
    mapping(
      "hundredthVolt" -> number,
      "reading" -> number,
    )(VoltToReading.apply)(VoltToReading.unapply)

  val meterMapping: Mapping[Meter] =
    mapping(
      "key" -> of[MeterKey],
      "faceName" -> of[MeterFaceName],
      "low" -> voltToReadingMapping,
      "high" -> voltToReadingMapping,
    )(Meter.apply)(Meter.unapply)

  val meterForm: Form[MeterStuff] = Form(
    mapping(
      "vRef" -> number,
      "meter" -> seq(meterMapping),
      "alarm" -> seq(alarmMapping)
    )(MeterStuff.apply)(MeterStuff.unapply)
  )
*/

  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val vRef: FieldEntry = dataStore.apply(FieldKey.apply("vRef", KeyFactory.commonKey())).get
      val meters: Seq[Meter] = dataStore(KeyKind.meterKey).map(_.value.asInstanceOf[Meter])
      val meterAlarms: Seq[MeterAlarm] = dataStore(KeyKind.meterAlarmKey).map(_.value.asInstanceOf[MeterAlarm])
      val value: FieldInt = vRef.value
      val meterStuff: MeterStuff = MeterStuff(value.value, meters, meterAlarms)

      Ok(views.html.meters(meterStuff))
  }

  def editMeter(meterKey: MeterKey): Action[AnyContent] = Action {
    Ok(s"todo: $meterKey")
  }
  def editAlarm(alarmKey: MeterAlarmKey): Action[AnyContent] = Action {
    Ok(s"todo: $alarmKey")
  }

  def save(): Action[AnyContent] = Action {
    implicit request =>


      /*meterForm.bindFromRequest.fold(
              formWithErrors => {
                BadRequest(views.html.meters(formWithErrors))
              },
        (meters: MeterStuff) => {
                val updateCandidate = UpdateCandidate(meters)
                dataStore.update(UpdateData(Seq(updateCandidate)))
    //        }
    //      )
  }*/
      Redirect(routes.MeterController.index)
  }
}

case class MeterStuff(vref: Int, meters: Seq[Meter], alarms: Seq[MeterAlarm])