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
import net.wa9nnn.rc210.KeyKind.clockKey
import net.wa9nnn.rc210.data.clock.{Clock, DSTPoint, MonthOfYearDST, Occurrence}
import net.wa9nnn.rc210.data.datastore.DataStoreActor.UpdateData
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.key.*
import net.wa9nnn.rc210.security.authorzation.AuthFilter.*
import net.wa9nnn.rc210.ui.{CandidateAndNames, FormParser}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.data.*
import play.api.data.Forms.*
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton()
class ClockController @Inject()(actor: ActorRef[DataStoreActor.Message])
                               (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds


//  private val dstPointForm: Mapping[DSTPoint] =
//    mapping(
//      "month" -> MonthOfYearDST.playForm,
//      "occurrence" -> of[Occurrence]
//    )(DSTPoint.apply)(DSTPoint.unapply)
//
//  private val clockForm = Form[Clock](
//    mapping(
//      "enableDST" -> boolean,
//      "hourDST" -> number(min = 0, max = 23),
//      "startDST" -> dstPointForm,
//      "endDST" -> dstPointForm,
//      "say24Hours" -> boolean
//    )(Clock.apply)(Clock.unapply))

  def index: Action[AnyContent] = Action.async {
    implicit request =>
      actor.ask[Seq[FieldEntry]](DataStoreActor.AllForKeyKind(clockKey, _)).map { fieldEntries =>
        val fieldEntry: FieldEntry = fieldEntries.head
        val clock: Clock = fieldEntry.value.asInstanceOf[Clock]
        Ok(views.html.clock(clock))
      }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    val candidateAndNames: CandidateAndNames = FormParser(Clock)
    actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
      Redirect(routes.ClockController.index)
    }
  }
}





