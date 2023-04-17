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
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.{DataStore, FormValue}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.KeyFactory.MeterKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.mvc._

import javax.inject._

class MeterEditorController @Inject()(val controllerComponents: ControllerComponents,
                                      dataStore: DataStore
                                     )(implicit namedManager: NamedManager)
  extends BaseController with LazyLogging {


  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val meterFields: Seq[FieldEntry] = dataStore(KeyKind.meterKey)
      val map: Map[FieldKey, FieldEntry] = meterFields
        .map(fieldEntry => fieldEntry.fieldKey -> fieldEntry).
        toMap
      val fieldNames: Seq[String] = meterFields.foldLeft(Set.empty[String]) { case (set: Set[String], fieldEntry) =>
        set + fieldEntry.fieldKey.fieldName
      }.toSeq
        .sorted

      val rows: Seq[Row] = for {
        fieldName <- fieldNames
      } yield {
        val cells: Seq[Cell] = for {
          number <- 1 to KeyKind.meterKey.maxN()
        } yield {
          map(FieldKey(fieldName, KeyFactory(KeyKind.meterKey, number))).toCell
        }
        Row(fieldName, cells: _*)
      }

      val colHeaders: Seq[Cell] = for {
        alarmKey <- KeyFactory[MeterKey](KeyKind.meterKey)
      } yield
        namedManager.get(alarmKey) match {
          case Some(value) =>
            Cell(value)
              .withToolTip(s"Alarm ${alarmKey.number}")

          case None => Cell(alarmKey.toString)
        }
      val header = Header(s"Alarms (${rows.length} values)", "Field" +: colHeaders: _*)
      val table = Table(header, rows)

      Ok(views.html.ports(table))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val kv: Map[String, String] = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }.filterNot(_._1 == "save")

      dataStore.simpleCandidate(kv.map { case (name, formValue) =>
        FormValue(name, formValue)
      })

      Redirect(routes.MeterEditorController.index())
  }
}