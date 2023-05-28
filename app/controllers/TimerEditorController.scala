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
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.{UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldInt}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.data.timers.Timer
import net.wa9nnn.rc210.key.KeyFactory.{Key, MacroKey, TimerKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import net.wa9nnn.rc210.util.MacroSelectField
import play.api.mvc._

import javax.inject._

class TimerEditorController @Inject()(val controllerComponents: ControllerComponents, datastore: DataStore)
  extends BaseController with LazyLogging {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val timerFields: Seq[FieldEntry] = datastore(KeyKind.timerKey)

      val rows: Seq[Row] = timerFields.map { fieldEntry =>
        val value: Timer = fieldEntry.value
        value.toRow
      }
      val header = Header(s"Timers (${rows.length} values)", "Timer", "Seconds", "Macro To Run")
      val table = Table(header, rows)

      Ok(views.html.timers(table))
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val namedKeyBuilder = Seq.newBuilder[NamedKey]

      val timers: Seq[UpdateCandidate] = request.body.asFormUrlEncoded
        .get
        .filterNot(_._1 == "save")
        .map { t: (String, Seq[String]) => FieldKey.fromParam(t._1) -> t._2.head }
        .groupBy(_._1.key)
        .map { case (key: Key, values: Map[FieldKey, String]) =>
          val valueMap = values.map { case (fk, value) =>
            fk.fieldName -> value
          }.toMap
          val name = valueMap("name")
          namedKeyBuilder += NamedKey(key, name)
          val seconds = FieldInt(valueMap("seconds").toInt)
          val macrotoRun: MacroKey = KeyFactory.apply(valueMap("macro"))
          val macroSelect = MacroSelectField(macrotoRun)
          val timer = Timer(key.asInstanceOf[TimerKey] , seconds = seconds, macroSelect = macroSelect)
          UpdateCandidate(timer.fieldKey, Right(timer))
        }.toSeq
      datastore.update(UpdateData(timers, namedKeyBuilder.result()))(who(request))
      Redirect(routes.TimerEditorController.index())
  }



}