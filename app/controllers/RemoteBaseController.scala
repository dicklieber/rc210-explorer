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
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.datastore.DataStoreActor.UpdateData
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.remotebase.RemoteBase
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.{CandidateAndNames, ProcessResult}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.data.Forms.*
import play.api.data.{Form, Mapping}
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton()
class RemoteBaseController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                    (implicit scheduler: Scheduler, ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    actor.ask[Seq[FieldEntry]](DataStoreActor.AllForKeyKind(KeyKind.remoteBaseKey, _)).map { fieldEntries =>
      val fieldEntry: FieldEntry = fieldEntries.head
      val remoteBase: RemoteBase = fieldEntry.value.asInstanceOf[RemoteBase]
      Ok(views.html.remoteBase(RemoteBase.form.fill(remoteBase)))
    }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    RemoteBase.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RemoteBase]) => {
          Future(BadRequest(views.html.remoteBase(formWithErrors)))
        },
        (remoteBase: RemoteBase) => {
          val updateCandidate: UpdateCandidate = UpdateCandidate(remoteBase)
          val candidateAndNames: CandidateAndNames = CandidateAndNames(updateCandidate, None)
          actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
            Redirect(routes.RemoteBaseController.index)
          }
        }
      )

  }

}



