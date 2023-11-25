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
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, ForFieldKey, Message}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldInt, FieldKey}
import net.wa9nnn.rc210.data.meter.{Meter, MeterAlarm}
import net.wa9nnn.rc210.ui.FormParser
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.data.Forms.*
import play.api.data.{Form, Mapping}
import play.api.mvc.*
import views.html

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class MeterController @Inject()(actor: ActorRef[Message])
                               (implicit scheduler: Scheduler, ec: ExecutionContext, cc: MessagesControllerComponents)
  extends AbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  //  private val alarmForm: Form[MeterAlarm] = Form(
  //    mapping(
  //      "key" -> of[MeterAlarmKey],
  //      "meter" -> of[MeterKey],
  //      "alarmType" -> of[AlarmType],
  //      "tripPoint" -> default(number, 0),
  //      "macroKey" -> of[MacroKey]
  //    )(MeterAlarm.apply)(MeterAlarm.unapply)
  //  )
  //
  //  private val voltToReadingMapping: Mapping[VoltToReading] =
  //    mapping(
  //      "hundredthVolt" -> number,
  //      "reading" -> number,
  //    )(VoltToReading.apply)(VoltToReading.unapply)
  //
  //  val meterForm: Form[Meter] = Form(
  //    mapping(
  //      "key" -> of[MeterKey],
  //      "faceName" -> of[MeterFaceName],
  //      "low" -> voltToReadingMapping,
  //      "high" -> voltToReadingMapping,
  //    )(Meter.apply)(Meter.unapply)
  //  )

  def index: Action[AnyContent] = Action.async {
    implicit request =>
      val eventualMaybeEntry: Future[Option[FieldEntry]] = actor.ask(ForFieldKey(FieldKey("vRef", Key(KeyKind.commonKey)), _))
      val eventualMeters: Future[Seq[FieldEntry]] = actor.ask(AllForKeyKind(KeyKind.meterKey, _))
      val eventualAlarmEntries: Future[Seq[FieldEntry]] = actor.ask(AllForKeyKind(KeyKind.meterAlarmKey, _))
      for
        maybeVref: Option[FieldEntry] <- eventualMaybeEntry
        metersEntries: Seq[FieldEntry] <- eventualMeters
        meterAlarmsEntries: Seq[FieldEntry] <- eventualAlarmEntries
      yield
        val vRef: Int = maybeVref.map(_.value.asInstanceOf[FieldInt].value).getOrElse(0)
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
          Ok(html.meter( meter))
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
          Ok(html.meterAlarm(meterAlarm))
        case None =>
          NotFound(s"Not meterKey: $fieldKey")

      }
  }

  def saveMeter(): Action[AnyContent] = Action.async {
    implicit request =>
      val formUrlEncoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
      val head: String = formUrlEncoded("key").head
      val meterKey: Key = Key(head)
      val name: String = formUrlEncoded("name").headOption.getOrElse("")

      throw new NotImplementedError()
  }

  def saveAlarm(): Action[AnyContent] = Action.async {
    implicit request =>
      val names = FormParser(Meter)
      throw new NotImplementedError()
  }
}

case class MeterStuff(vref: Int, meters: Seq[Meter], alarms: Seq[MeterAlarm])