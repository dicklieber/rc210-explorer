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
import net.wa9nnn.rc210.data.courtesy.{CourtesyTone, CtField}
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.data.datastore.DataStoreActor.UpdateData
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.{CandidateAndNames, ProcessResult}
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.MessagesControllerComponents

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import play.api.i18n.Messages.implicitMessagesProviderToMessages

class CourtesyToneEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                            (implicit scheduler: Scheduler, ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging
    with play.api.i18n.I18nSupport {
  implicit val timeout: Timeout = 3 seconds

  def index(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>

      val future: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKeyKind(KeyKind.courtesyToneKey, _))
      future.map { (entries: Seq[FieldEntry]) =>

        val courtesyTones: Seq[CourtesyTone] = entries.map(_.value.asInstanceOf[CourtesyTone])

        Ok(views.html.courtesyTones(courtesyTones))
      }
  }


  def edit(key: Key): Action[AnyContent] = Action.async {
    implicit request =>
      key.check(KeyKind.courtesyToneKey)
      val future: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKey(key, _))
      future.map { (entries: Seq[FieldEntry]) =>
        val courtesyToneEntry: FieldEntry = entries.head
        val courtesyTone: CourtesyTone = courtesyToneEntry.value
        implicit val form: Form[CourtesyTone] = CourtesyTone.form.fill(courtesyTone)
        logger.whenDebugEnabled {
          val data = form.data
          data.foreach { (key, value) =>
            logger.debug("{} => {}", key, value)
          }
        }
        Ok(views.html.courtesyToneEdit(key.namedKey))
      }
  }


  def save(): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      CourtesyTone.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[CourtesyTone]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey
            Future(BadRequest(views.html.courtesyToneEdit(namedKey)(formWithErrors, request, request)))
          },
          (courtesyTone: CourtesyTone) => {
            val candidateAndNames = ProcessResult(courtesyTone)
            actor.ask[String](UpdateData(candidateAndNames, user, _)).map { _ =>
              Redirect(routes.CourtesyToneEditorController.index())
            }
          }
        )
  }
}