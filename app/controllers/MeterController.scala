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
import net.wa9nnn.rc210.data.datastore.{AllForKeyKind, DataStore, ForFieldKey}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldInt, FieldKey}
import net.wa9nnn.rc210.data.meter.*
import net.wa9nnn.rc210.data.timers.Timer
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.ProcessResult
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.data.Forms.*
import play.api.data.{Form, Mapping}
import play.api.mvc.*
import play.api.mvc.*
import views.html

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class MeterController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index: Action[AnyContent] = Action {
    implicit request =>
      /*      val eventualMaybeEntry: Future[Option[FieldEntry]] = actor.ask(ForFieldKey(FieldKey("vRef", Key(KeyKind.commonKey)), _))
            val eventualMeters: Future[Seq[FieldEntry]] = actor.ask(AllForKeyKind(KeyKind.meterKey, _))
            val eventualAlarmEntries: Future[Seq[FieldEntry]] = actor.ask(AllForKeyKind(KeyKind.meterAlarmKey, _))
            for
              maybeVref: Option[FieldEntry] <- eventualMaybeEntry
              metersEntries: Seq[FieldEntry] <- eventualMeters
              meterAlarmsEntries: Seq[FieldEntry] <- eventualAlarmEntries
            yield*/
      val vRef: Int = dataStore(ForFieldKey(FieldKey("vRef", Key(KeyKind.commonKey)))).head[FieldInt].value
      val meters: Seq[Meter] = metersEntries.map { fe => fe.value.asInstanceOf[Meter] }
      val meterAlarms: Seq[MeterAlarm] = meterAlarmsEntries.map { (fe: FieldEntry) => fe.value.asInstanceOf[MeterAlarm] }
      val meterStuff = MeterStuff(vRef, meters, meterAlarms)
      Ok(html.meters(meterStuff))
  }

  def editMeter(meterKey: Key): Action[AnyContent] = Action.async {
    implicit request =>

      val fieldKey = FieldKey("Meter", meterKey)
      actor.ask(ForFieldKey(fieldKey, _)).map {
        case Some(fieldEntry: FieldEntry) =>
          val meter: Meter = fieldEntry.value

          Ok(html.meterEditor(Meter.meterForm.fill(meter), meterKey.namedKey))
        case None =>
          NotFound(s"Not meterKey: $fieldKey")
      }
  }

  def editAlarm(alarmKey: Key): Action[AnyContent] = Action.async {
    implicit request =>
      val fieldKey = FieldKey("MeterAlarm", alarmKey)
      actor.ask(ForFieldKey(fieldKey, _)).map {
        case Some(fieldEntry: FieldEntry) =>
          val meterAlarm: MeterAlarm = fieldEntry.value

          val form = MeterAlarm.form.fill(meterAlarm)
          Ok(html.meterAlarm(form, alarmKey.namedKey))
        case None =>
          NotFound(s"Not meterKey: $fieldKey")
      }
  }

  def saveMeter(): Action[AnyContent] = Action.async {
    implicit request =>
      meterForm
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[Meter]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey
            Future(BadRequest(html.meterEditor(formWithErrors, namedKey)))
          },
          (meter: Meter) => {
            val candidateAndNames = ProcessResult(meter)
            actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
              val index1: Call = routes.MeterController.index
              Redirect(index1)
            }
          }
        )
  }

  def saveAlarm(): Action[AnyContent] = Action.async {
    implicit request =>
      MeterAlarm.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[MeterAlarm]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey
            Future(BadRequest(html.meterAlarm(formWithErrors, namedKey)))
          },
          (meterAlarm: MeterAlarm) => {
            val candidateAndNames = ProcessResult(meterAlarm)
            actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
              Redirect(routes.MeterController.index)
            }
          }
        )
  }
}

case class MeterStuff(vref: Int, meters: Seq[Meter], alarms: Seq[MeterAlarm])