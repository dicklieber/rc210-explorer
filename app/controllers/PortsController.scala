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
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.datastore.DataStoreActor.AllForKeyKind
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey, FieldValue, SimpleFieldValue}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.SimpleValuesHandler
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class PortsController @Inject()(implicit actor: ActorRef[DataStoreActor.Message], scheduler: Scheduler, ec: ExecutionContext, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private var simpleValuesHandler: Option[SimpleValuesHandler] = None


  def index(): Action[AnyContent] = Action.async {
    implicit request => {
      val future: Future[Seq[FieldEntry]] = actor.ask(AllForKeyKind(KeyKind.portKey, _))
      future.map { (fieldEntries: Seq[FieldEntry]) =>
        if (simpleValuesHandler.isEmpty)
          simpleValuesHandler = Some(new SimpleValuesHandler(fieldEntries))


        val rows: Seq[Row] = fieldEntries
          .groupBy(_.fieldKey.fieldName)
          .toSeq
          .sortBy(_._1)
          .map { (name, portEntries) =>
            Row(name, portEntries.sortBy(_.fieldKey.key.rc210Value))
          }

        Ok(views.html.ports(rows))
      }
    }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      val (updateCandidates, namedKeys) = simpleValuesHandler.get.collect
      actor.ask[String](DataStoreActor.UpdateData(updateCandidates, namedKeys, user, _)).map { _ =>
        Redirect(routes.PortsController.index())
      }
  }
}

case class Row(name: String, portEntries: Seq[FieldEntry]) {
  def cells: Seq[String] = {
    portEntries.map { fe =>
      val v: FieldValue = fe.value
      v.toHtmlField(fe.fieldKey)
    }
  }
}