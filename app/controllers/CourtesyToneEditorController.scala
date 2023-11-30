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
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.CandidateAndNames
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import javax.inject.*
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class CourtesyToneEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                            (implicit scheduler: Scheduler, ec: ExecutionContext, cc: ControllerComponents)
  extends AbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds

  def index(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>

      val future: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKeyKind(KeyKind.courtesyToneKey, _))
      future.map { (entries: Seq[FieldEntry]) =>
        val rows: Seq[Seq[CtField]] = entries.flatMap { fieldEntry =>
          val courtesyTone = fieldEntry.value.asInstanceOf[CourtesyTone]
          courtesyTone.rows
        }

        Ok(views.html.courtesyTones(rows))
      }
  }

  def edit(key: Key): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      key.check(KeyKind.courtesyToneKey)
      val future: Future[Seq[FieldEntry]] = actor.ask(DataStoreActor.AllForKey(key, _))
      future.map { (entries: Seq[FieldEntry]) =>
        val courtesyToneEntry: FieldEntry = entries.head
        val value: CourtesyTone = courtesyToneEntry.value
        val rows: Seq[Seq[CtField]] = value.rows
        Ok(views.html.courtesyToneEdit(rows, key))
      }

  }

  def save(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      Future(ImATeapot)
    //      val ex = FormExtractor(request)
    //      val candidateAndNames: CandidateAndNames = FormParser(CourtesyTone)
    //      actor.ask[String](DataStoreActor.UpdateData(candidateAndNames, user, _)).map { _ =>
    //        Redirect(routes.CourtesyToneEditorController.index())
    //      }

    /*      val namedKeyBuilder = Seq.newBuilder[NamedKey]
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
            .map { case (ctKey: Key, values: Map[CtSegmentKey, String]) =>
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

          actor.ask[String](DataStoreActor.UpdateData(ctBuilder.result(), namedKeyBuilder.result(), user = user(request), _)).map { _ =>
            Redirect(routes.CourtesyToneEditorController.index())
          }
    */
  }
}