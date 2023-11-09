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
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldInt}
import net.wa9nnn.rc210.data.meter.{AlarmType, Meter, MeterAlarm, MeterFaceName, VoltToReading}
import net.wa9nnn.rc210.key._
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
//import net.wa9nnn.rc210.data.FieldKey
//import net.wa9nnn.rc210.data.datastore.DataStoreActor.ForFieldKey
//import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
//import net.wa9nnn.rc210.data.field.Formatters._
//import net.wa9nnn.rc210.data.field.{FieldEntry, FieldInt}
//import net.wa9nnn.rc210.data.meter._
//import net.wa9nnn.rc210.data.named.NamedKey
//import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, MeterAlarmKey, MeterKey}
//import net.wa9nnn.rc210.key.KeyFormats._
//import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
//import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc._
import views.html

import javax.inject._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class MeterController @Inject()(actor: ActorRef[DataStoreActor.Message])
                               (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  private val alarmForm: Form[MeterAlarm] = Form(
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

  def index: Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      for {
        maybeVref: Option[FieldEntry] <- actor.ask(DataStoreActor.ForFieldKey(FieldKey("vRef", KeyFactory.commonKey()), _))
        metersEntries: Seq[FieldEntry] <- actor.ask(DataStoreActor.AllForKeyKind(KeyKind.meterKey, _))
        meterAlarmsEntries: Seq[FieldEntry] <- actor.ask(DataStoreActor.AllForKeyKind(KeyKind.meterAlarmKey, _))
      } yield {
        val vRef: Int = maybeVref.map(_.value.asInstanceOf[FieldInt]).getOrElse(FieldInt(0)).value
        val meters: Seq[Meter] = metersEntries.map { fe => fe.value.asInstanceOf[Meter] }
        val meterAlarms: Seq[MeterAlarm] = meterAlarmsEntries.map { fe => fe.value.asInstanceOf[MeterAlarm] }
        val meterStuff: MeterStuff = MeterStuff(vRef, meters, meterAlarms)
        Ok(html.meters(meterStuff))
      }
  }

  def editMeter(meterKey: MeterKey): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("Meter", meterKey)
      actor.ask(ForFieldKey(fieldKey, _)).map {
        case Some(fieldEntry: FieldEntry) =>
          val meter: Meter = fieldEntry.value
          val fillInForm = meterForm.fill(meter)
          Ok(html.meter(meterKey, fillInForm))
        case None =>
          NotFound(s"Not meterKey: $fieldKey")
      }
  }

  def editAlarm(alarmKey: MeterAlarmKey): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("MeterAlarm", alarmKey)
      actor.ask(ForFieldKey(fieldKey, _)).map {
        case Some(fieldEntry: FieldEntry) =>
          val meterAlarm: MeterAlarm = fieldEntry.value
          val fillInForm = alarmForm.fill(meterAlarm)
          Ok(html.meterAlarm(meterAlarm.key, fillInForm))
        case None =>
          NotFound(s"Not meterKey: $fieldKey")

      }
  }

  def saveMeter(): Action[AnyContent] = Action.async {
    implicit request =>
      val formUrlEncoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
      val meterKey: MeterKey = KeyFactory(formUrlEncoded("key").head)
      val name: String = formUrlEncoded("name").headOption.getOrElse("")

      meterForm.bindFromRequest().fold(
        formWithErrors => {
          val maybeString = formWithErrors.data.get("key")
          val meterKey: MeterKey = maybeString.map(KeyFactory(_)).get
          Future(BadRequest(html.meter(meterKey, formWithErrors)))
        },
        (meter: Meter) => {
          actor.ask[String](DataStoreActor.UpdateData(Seq(UpdateCandidate(meter)), Seq(NamedKey(meterKey, name)),
            who(request), _)).map { _ =>
            Redirect(routes.MeterController.index)
          }
        }
      )
  }

  def saveAlarm(): Action[AnyContent] = Action.async {
    implicit request =>
      val formUrlEncoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
      val meterAlarmKey: MeterAlarmKey = KeyFactory(formUrlEncoded("key").head)
      val name: String = formUrlEncoded("name").headOption.getOrElse("")
      alarmForm.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(html.meterAlarm(meterAlarmKey, formWithErrors)))
        },
        (meterAlarm: MeterAlarm) => {

          actor.ask[String](DataStoreActor.UpdateData(Seq(UpdateCandidate(meterAlarm)), Seq(NamedKey(meterAlarmKey, name)),
            who(request), _)).map { _ =>
            Redirect(routes.MeterController.index)
          }
        }
      )
  }
}

case class MeterStuff(vref: Int, meters: Seq[Meter], alarms: Seq[MeterAlarm])