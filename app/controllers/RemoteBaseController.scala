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
import net.wa9nnn.rc210.data.datastore.DataStoreActor.AllForKeyKind
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.remotebase.*
import net.wa9nnn.rc210.data.remotebase.Mode.*
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
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
                                    (implicit scheduler: Scheduler, ec: ExecutionContext) extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds


  private val rbMemory: Mapping[RBMemory] =
    mapping(
      "frequency" -> text,
      "offset" -> of[Offset],
      "mode" -> of[Mode],
      "ctcssMode" -> of[CtcssMode],
      "ctssCode" -> number,
    )(RBMemory.apply)(RBMemory.unapply)

  val remoteBaseForm: Form[RemoteBase] = Form[RemoteBase](
    mapping(
      "radio" -> of[Radio],
      "yaesu" -> of[Yaesu],
      "prefix" -> text,
      "memories" -> seq(rbMemory),
    )(RemoteBase.apply)(RemoteBase.unapply)
  )

  def index: Action[AnyContent] = Action.async { implicit request =>
    actor.ask(AllForKeyKind(KeyKind.remoteBaseKey, _)).map { (fields: Seq[FieldEntry]) =>
      val remoteBase: RemoteBase = fields.head.value.asInstanceOf[RemoteBase]
      val filledInForm = remoteBaseForm.fill(remoteBase)
      Ok(views.html.rermoteBase(filledInForm))
    }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>

    remoteBaseForm.bindFromRequest().fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        Future(BadRequest(views.html.rermoteBase(formWithErrors)))
      },
      (remoteBase: RemoteBase) => {
        /* binding success, you get the actual value. */
        val updateCandidate = UpdateCandidate(remoteBase.fieldKey, Right(remoteBase))

        actor.ask[String](DataStoreActor.UpdateData(Seq(updateCandidate), Seq.empty,
          who(request), _)).map { _ =>
          Redirect(routes.RemoteBaseController.index)
        }
      }
    )
  }

}



