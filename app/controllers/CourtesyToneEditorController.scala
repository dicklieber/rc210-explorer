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
import net.wa9nnn.rc210.data.courtesy.{CourtesyTone, CtSegmentKey}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.KeyKind
import play.api.mvc._

import javax.inject._

class CourtesyToneEditorController @Inject()(val controllerComponents: ControllerComponents,
                                             mappedValues: MappedValues
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
      val kv: Map[String, String] = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }.filterNot(_._1 == "save")
      val ckv: Map[CtSegmentKey, String] = for {
        case (key, value) <- kv
      } yield {
        CtSegmentKey(key) -> value
      }
      //todo assemble CourtesyTone from ckv fields
      // todo handle name change.
      Redirect(routes.CourtesyToneEditorController.index())
  }
}