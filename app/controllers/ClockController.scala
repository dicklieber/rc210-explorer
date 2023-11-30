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
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.KeyKind.clockKey
import net.wa9nnn.rc210.data.clock.DSTPoint.dstPointForm
import net.wa9nnn.rc210.data.clock.{Clock, DSTPoint, MonthOfYearDST, Occurrence}
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.*
import net.wa9nnn.rc210.data.field.FieldEntry
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.{CandidateAndNames, ProcessResult}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.MessagesControllerComponents
import play.twirl.api.Html

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


@Singleton()
class ClockController @Inject()(implicit actor: ActorRef[DataStoreActor.Message],
                                ec: ExecutionContext,
                                scheduler: Scheduler,
                                components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds


  private val clockForm = Form[Clock](
    mapping(
      "key" -> of[Key],
      "enableDST" -> boolean,
      "hourDST" -> number(min = 0, max = 23),
      "startDST" -> dstPointForm,
      "endDST" -> dstPointForm,
      "say24Hours" -> boolean
    )(Clock.apply)(Clock.unapply))

  def index: Action[AnyContent] = Action.async {
    implicit request =>
      actor.ask[Seq[FieldEntry]](DataStoreActor.AllForKeyKind(clockKey, _)).map { fieldEntries =>
        val fieldEntry: FieldEntry = fieldEntries.head
        val clock: Clock = fieldEntry.value.asInstanceOf[Clock]

        Ok(views.html.clock(clockForm.fill(clock)))
      }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    clockForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Clock]) => {
          Future(BadRequest(views.html.clock(formWithErrors)))
        },
        (clock: Clock) => {
          val candidateAndNames = ProcessResult(clock)
          actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
            Redirect(routes.ClockController.index)
          }

        }
      )
  }
}





