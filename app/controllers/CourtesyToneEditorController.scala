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
import com.wa9nnn.util.tableui.Row
import net.wa9nnn.rc210.data.courtesy.{CourtesyTone, CtSegmentKey, Segment}
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.key.{CourtesyToneKey, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class CourtesyToneEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                            (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>

      actor.ask(DataStoreActor.AllForKeyKind(KeyKind.courtesyToneKey, _)).map { entries =>
        //        val rows: Seq[Row] = entries.map { fe =>
        //          val ct: CourtesyTone = fe.value
        //          ct.rows()
        //        }
        //        Ok(views.html.courtesyTones(rows))
        Ok("todo")
      }
  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      val namedKeyBuilder = Seq.newBuilder[NamedKey]
      val ctBuilder = Seq.newBuilder[UpdateCandidate]


      val form = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }

      form.filter(_._1 startsWith "name")
        .foreach { case (sKey, value) =>
          val ctKey = CtSegmentKey(sKey)
          namedKeyBuilder += NamedKey(ctKey.ctKey, value)
        }


      form
        .filterNot(_._1 == "save")
        .filterNot(_._1 startsWith "name")
        .map { case (sKey, value) => CtSegmentKey(sKey) -> value } // convert from string name to CtSegmentKeys
        .groupBy(_._1.ctKey)
        .map { case (ctKey: CourtesyToneKey, values: Map[CtSegmentKey, String]) =>
          val segments = values
            .groupBy(_._1.segment).map { case (seg, values) =>
              val valuesForSegment: Map[String, String] = values.map { case (crSKey: CtSegmentKey, value: String) =>
                crSKey.name -> value
              }
              // we now have a map of names to values
              Segment(valuesForSegment)
            }
          val courtesyTone = CourtesyTone(ctKey, segments.toSeq)
          ctBuilder += UpdateCandidate(courtesyTone)
        }

      actor.ask[String](DataStoreActor.UpdateData(ctBuilder.result(), namedKeyBuilder.result(), user = who(request), _)).map { _ =>
        Redirect(routes.CourtesyToneEditorController.index())
      }
  }
}