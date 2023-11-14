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

import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.AllForKeyKind
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.logicAlarm.LogicAlarm
import net.wa9nnn.rc210.key.*
import net.wa9nnn.rc210.ui.{CandidateAndNames, FormParser}
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import play.api.i18n.*
import net.wa9nnn.rc210.data.datastore.DataStoreActor.UpdateData
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import net.wa9nnn.rc210.data.field.Formatters.*
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user

@Singleton
class LogicAlarmEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                          (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds


  //  private val logicForm: Form[LogicAlarm] = Form(
  //    mapping(
  //      "key" -> of[LogicAlarmKey],
  //      "enable" -> boolean,
  //      "lowMacro" -> of[MacroKey],
  //      "highMacro" -> of[MacroKey]
  //    )(LogicAlarm.apply)(LogicAlarm.unapply)
  //  )

  def index(): Action[AnyContent] = Action.async {
    implicit request =>
      actor.ask[Seq[FieldEntry]](AllForKeyKind(KeyKind.logicAlarmKey, _)).map { alarmFields =>
        val logicAlarms: Seq[LogicAlarm] = alarmFields.map(_.value.asInstanceOf[LogicAlarm])
        Ok(views.html.logic(logicAlarms))
      }
  }

  def edit(logicAlarmKey: LogicAlarmKey) = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey("LogicAlarm", logicAlarmKey)
      actor.ask[Option[FieldEntry]](DataStoreActor.ForFieldKey(fieldKey, _)).map {
        {
          case Some(fieldEntry) =>
            val logicAlarm: LogicAlarm = fieldEntry.value
            Ok(views.html.logicEditor(logicAlarm))

          case None =>
            NotFound(s"No key: $logicAlarmKey")
        }
      }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>

      val encoded = AnyContentAsFormUrlEncoded(request.body.asFormUrlEncoded.get)
      val candidateAndNames: CandidateAndNames =
        FormParser(encoded, (valuesMap: Map[String, String]) => {
          LogicAlarm(valuesMap)
        }
        )

      //      val candidateAndNames: CandidateAndNames = FormParser(encoded)
      actor.ask[String](UpdateData(candidateAndNames, user = user, _)).map { _ =>
        Redirect(routes.LogicAlarmEditorController.index())
      }
  }
}