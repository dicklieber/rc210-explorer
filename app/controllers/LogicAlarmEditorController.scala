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
import net.wa9nnn.rc210.data.datastore.{DataStore, FormValue, UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.key.KeyFactory.LogicAlarmKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import play.api.mvc._

import javax.inject._
import scala.collection.immutable

class LogicAlarmEditorController @Inject()(val controllerComponents: ControllerComponents, dataStore: DataStore)
  extends BaseController with LazyLogging {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val alarmFields: Seq[FieldEntry] = dataStore(KeyKind.logicAlarmKey)
      val map: Map[FieldKey, FieldEntry] = alarmFields
        .map(fieldEntry => fieldEntry.fieldKey -> fieldEntry).
        toMap
      val fieldNames: Seq[String] = alarmFields.foldLeft(Set.empty[String]) { case (set: Set[String], fieldEntry) =>
        set + fieldEntry.fieldKey.fieldName
      }.toSeq
        .sorted

      val rows: Seq[Row] = for {
        fieldName <- fieldNames
      } yield {
        val cells: Seq[Cell] = for {
          number <- 1 to KeyKind.logicAlarmKey.maxN()
        } yield {
          val fieldKey = FieldKey(fieldName, KeyFactory(KeyKind.logicAlarmKey, number))
          val fieldEntry = map(fieldKey)
          fieldEntry.toCell
        }

        Row(fieldName, cells: _*)
      }

      val colHeaders: Seq[Cell] = for {
        alarmKey <- KeyFactory[LogicAlarmKey](KeyKind.logicAlarmKey)
      } yield {
        alarmKey.namedCell()
      }
      val header = Header(s"Logic Alarms (${rows.length} values)", "Field" +: colHeaders: _*)
      val table = Table(header, rows)

      Ok(views.html.logic(table))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val kv: Map[String, String] = request.body.asFormUrlEncoded.get.map { t => t._1 -> t._2.head }.filterNot(_._1 == "save")

     val r: Seq[UpdateCandidate] =  kv.map { case (name, formValue) =>
        val fieldKey = FieldKey.fromParam(name)
       UpdateCandidate(fieldKey, Left(formValue))
      }.toSeq
      dataStore.update(UpdateData(r)) // todo handle name.

      Redirect(routes.LogicAlarmEditorController.index())
  }
}