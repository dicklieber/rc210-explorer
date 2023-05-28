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
import net.wa9nnn.rc210.data.datastore.{UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.Formatters._
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldInt}
import net.wa9nnn.rc210.data.meter._
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, MeterAlarmKey, MeterKey}
import net.wa9nnn.rc210.key.KeyFormats._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc._
import views.html

import javax.inject._

class MeterController @Inject()(dataStore: DataStore) extends MessagesInjectedController with LazyLogging {

  val alarmForm: Form[MeterAlarm] = Form(
    mapping(
      "key" -> of[MeterAlarmKey],
      "meter" -> of[MeterKey],
      "alarmType" -> of[AlarmType],
      "tripPoint" -> default(number, 0),
      "macroKey" -> of[MacroKey]
    )(MeterAlarm.apply)(MeterAlarm.unapply)
  )

  private val voltToReadingMapping: Mapping[VoltToReading] =
    mapping(
      "hundredthVolt" -> number,
      "reading" -> number,
    )(VoltToReading.apply)(VoltToReading.unapply)

  val meterForm: Form[Meter] = Form(
    mapping(
      "key" -> of[MeterKey],
      "faceName" -> of[MeterFaceName],
      "low" -> voltToReadingMapping,
      "high" -> voltToReadingMapping,
    )(Meter.apply)(Meter.unapply)
  )

  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val vRef: FieldEntry = dataStore.apply(FieldKey.apply("vRef", KeyFactory.commonKey())).get
      val meters: Seq[Meter] = dataStore(KeyKind.meterKey).map(_.value.asInstanceOf[Meter])
      val meterAlarms: Seq[MeterAlarm] = dataStore(KeyKind.meterAlarmKey).map(_.value.asInstanceOf[MeterAlarm])
      val value: FieldInt = vRef.value
      val meterStuff: MeterStuff = MeterStuff(value.value, meters, meterAlarms)
      Ok(html.meters(meterStuff))
  }

  def editMeter(meterKey: MeterKey): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("Meter", meterKey)
      val fieldEntry: FieldEntry = dataStore(fieldKey).get
      val meter: Meter = fieldEntry.value
      val fillInForm = meterForm.fill(meter)
      Ok(html.meter(meterKey, fillInForm))
  }

  def editAlarm(alarmKey: MeterAlarmKey): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("MeterAlarm", alarmKey)
      val fieldEntry: FieldEntry = dataStore(fieldKey).get
      val meter: MeterAlarm = fieldEntry.value
      val fillInForm = alarmForm.fill(meter)
      Ok(html.meterAlarm(alarmKey, fillInForm))

  }

  def saveMeter(): Action[AnyContent] = Action {
    implicit request =>
      val formUrlEncoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
      val meterKey: MeterKey = KeyFactory(formUrlEncoded("key").head)
      val name: String = formUrlEncoded("name").headOption.getOrElse("")

      meterForm.bindFromRequest.fold(
        formWithErrors => {
          val maybeString = formWithErrors.data.get("key")
          val meterKey: MeterKey = maybeString.map(KeyFactory(_)).get
          BadRequest(html.meter(meterKey, formWithErrors))
        },
        (meter: Meter) => {
          val updateCandidate: UpdateCandidate = UpdateCandidate(meter)
          val updateData = UpdateData(Seq(updateCandidate), Seq(NamedKey(meterKey, name)))
          dataStore.update(updateData)(who(request))
          //        }
          //      )
          Redirect(routes.MeterController.index)
        }
      )
  }

  def saveAlarm(): Action[AnyContent] = Action {
    implicit request =>
      val formUrlEncoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
      val meterAlarmKey: MeterAlarmKey = KeyFactory(formUrlEncoded("key").head)
      val name: String = formUrlEncoded("name").headOption.getOrElse("")
      alarmForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(html.meterAlarm(meterAlarmKey, formWithErrors))
        },
        (meterAlarm: MeterAlarm) => {
          val updateCandidate: UpdateCandidate = UpdateCandidate(meterAlarm)
          val updateData = UpdateData(Seq(updateCandidate), Seq(NamedKey(meterAlarmKey, name)))
          dataStore.update(updateData)(who(request))
          //        }
          //      )
          Redirect(routes.MeterController.index)
        }
      )
    }
}

case class MeterStuff(vref: Int, meters: Seq[Meter], alarms: Seq[MeterAlarm])