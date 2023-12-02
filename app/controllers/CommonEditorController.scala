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
import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.{MessagesControllerComponents, *}

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class CommonEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                      (implicit scheduler: Scheduler,
                                       ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components)
    with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private var fieldKeys: Seq[FieldKey] = Seq.empty

  def index(): Action[AnyContent] = Action.async {
    implicit request =>
      val eventualFieldEntries: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKeyKind(KeyKind.commonKey, _))

      eventualFieldEntries.map { fieldEntries =>
        if (fieldKeys.isEmpty)
          fieldKeys = fieldEntries.map(_.fieldKey)
        Ok(views.html.common(fieldEntries))
      }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>

      val data: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
      logger.whenTraceEnabled {
        data.foreach { entry =>
          logger.trace(entry.toString())
        }
      }

      val uc: Seq[UpdateCandidate] = fieldKeys.map { fieldKey =>
        val v: Option[String] = for {
          values <- data.get(fieldKey.toString)
          value <- values.headOption
        } yield {
          value
        }
        val str: String = v.getOrElse("")

        logger.debug("fieldKey: {} => value: {}", fieldKey.toString, str)
        UpdateCandidate(fieldKey, Left(str))
      }

      logger.whenTraceEnabled {
        uc.foreach { uc =>
          logger.trace(uc.toString)
        }
      }

      actor.ask[String](DataStoreActor.UpdateData(uc, Seq.empty, user, _)).map { _ =>
        Redirect(routes.CommonEditorController.index())
      }
  }
}
