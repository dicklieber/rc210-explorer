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
import com.wa9nnn.util.tableui.Row
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.courtesy.{CourtesyTone, CtSegmentKey, Segment}
import net.wa9nnn.rc210.data.datastore.{DataStore, UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.key.KeyFactory.CourtesyToneKey
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.ui.FormParser
import play.api.mvc._

import javax.inject._

class CourtesyToneEditorController @Inject()(val controllerComponents: ControllerComponents,
                                             dataStore: DataStore
                                            )
  extends BaseController with LazyLogging {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val entries: Seq[FieldEntry] = dataStore(KeyKind.courtesyToneKey)
      val rows: Seq[Row] = entries.flatMap { fe: FieldEntry =>
        val ct: CourtesyTone = fe.value
        ct.rows()
      }
      Ok(views.html.courtesyTones(rows))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val namedKeyBuilder = Seq.newBuilder[NamedKey]
      val ctBuilder = Seq.newBuilder[UpdateCandidate]


      val form = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }

      form.filter(_._1 startsWith ("name"))
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
          val segs = values
            .groupBy(_._1.segment).map { case (seg, values) =>
            val valuesForSegment: Map[String, String] = values.map { case (crSKey: CtSegmentKey, value: String) =>
              crSKey.name -> value
            }
            // we now have a map of names to values
            Segment(valuesForSegment)
          }
          val courtesyTone = CourtesyTone(ctKey, segs.toSeq)
          ctBuilder += UpdateCandidate(courtesyTone)
        }
      dataStore.update(UpdateData(ctBuilder.result(), namedKeyBuilder.result()))
      Redirect(routes.CourtesyToneEditorController.index())
  }
}