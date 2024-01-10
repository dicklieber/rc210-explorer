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
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{ComplexExtractor, FieldEntry, FieldInt}
import net.wa9nnn.rc210.data.meter.*
import net.wa9nnn.rc210.NamedKey
import net.wa9nnn.rc210.security.Who.request2Session
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.{ComplexFieldController, ProcessResult}
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Forms.*
import play.api.data.{Form, Mapping}
import play.api.mvc.*
import views.html

import javax.inject.*

class MeterController @Inject()(dataStore: DataStore, components: MessagesControllerComponents)
  extends ComplexFieldController[MeterNode](dataStore, components) with LazyLogging {

  override val complexExtractor: ComplexExtractor[MeterNode] = MeterNode

  override def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>

      val vRefEntry: FieldInt = dataStore.editValue(FieldKey("vRef", Key(KeyKind.Common, 1)))
      val vRef: Int = vRefEntry.value
      val meters: Seq[MeterNode] = dataStore.indexValues(KeyKind.Meter)
      val meterAlarms: Seq[MeterAlarm] = dataStore.indexValues(KeyKind.MeterAlarm)
      val meterStuff = MeterStuff(vRef, meters, meterAlarms)
      Ok(html.meters(meterStuff))
  }

  override def indexResult(values: Seq[MeterNode]): Result = {
    throw new NotImplementedError("cauae we override index")
  }

  override def editResult(filledForm: Form[MeterNode], namedKey: NamedKey)(using request: MessagesRequest[AnyContent]): Result =
    Ok(views.html.meterEditor(filledForm, namedKey))

  override def saveOkResult(): Result =
    Redirect(routes.MeterController.index)

}

case class MeterStuff(vref: Int, meters: Seq[MeterNode], alarms: Seq[MeterAlarm])