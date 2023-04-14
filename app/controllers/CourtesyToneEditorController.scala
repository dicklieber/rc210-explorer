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
import net.wa9nnn.rc210.data.courtesy.{CourtesyTone, CtSegmentKey, Segment}
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.named.{NamedKey, NamedManager}
import net.wa9nnn.rc210.key.KeyFactory.CourtesyToneKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.mvc._

import javax.inject._
import scala.collection.immutable

class CourtesyToneEditorController @Inject()(val controllerComponents: ControllerComponents,
                                             mappedValues: DataStore
                                            )(implicit namedManager: NamedManager)
  extends BaseController with LazyLogging {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val entries: Seq[FieldEntry] = mappedValues(KeyKind.courtesyToneKey)
      val rows: Seq[Row] = entries.flatMap { fe =>
        val ct: CourtesyTone = fe.value
        ct.rows()
      }
      Ok(views.html.courtesyTones(rows))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }
        .filterNot(_._1 == "save")
        .map { case (sKey, value) => CtSegmentKey(sKey) -> value } // convert from string name to CtSegmentKeys
        .groupBy(_._1.ctKey)
        .map { case (key, values) =>
          val segs: Seq[Segment] = values.groupBy(_._1.segment).flatMap { case (seg, values) =>
            val valuesForSegement: Map[String, String] = values.map { case (crSKey, value) => crSKey.name -> value }
            // we now have a map of names to values
            if (valuesForSegement.size == 1) {
              val str = valuesForSegement("name")
              namedManager.update(Seq(NamedKey(key, str)))
              Seq.empty
            } else
              Seq(Segment(valuesForSegement))
          }.toSeq
          CourtesyTone(key, segs)
        }

      Redirect(routes.CourtesyToneEditorController.index())
  }
}