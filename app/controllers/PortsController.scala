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
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.ui.{ProcessResult, SimpleValuesHandler}
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import net.wa9nnn.rc210.security.Who.request2Session
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey

@Singleton
class PortsController @Inject()(implicit dataStore: DataStore, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {
  private var simpleValuesHandler: Option[SimpleValuesHandler] = None

  def index(): Action[AnyContent] = Action {
    implicit request => {
      val fieldEntries = dataStore.apply(KeyKind.Port)
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

  def save(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val collect: CandidateAndNames = simpleValuesHandler.get.collect
      val candidateAndNames = ProcessResult(collect)

      given RcSession = request.attrs(sessionKey)

      dataStore.update(candidateAndNames)
      Redirect(routes.PortsController.index())
  }
}

/**
 * One <tr> of data used by [[views.html.ports]]
 *
 * @param name        row header 
 * @param portEntries remaining td elements. 
 */
case class Row(name: String, portEntries: Seq[FieldEntry]) {
  def cells: Seq[String] = {
    portEntries.map { fe =>
      val v: FieldValue = fe.value
      v.toHtmlField(fe.fieldKey)
    }
  }
}