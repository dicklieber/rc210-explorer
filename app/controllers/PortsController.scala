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
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey, FieldValue, SimpleFieldValue}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.ui.SimpleValuesHandler
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class PortsController @Inject()(implicit dataStore: DataStore, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {
  implicit val timeout: Timeout = 3 seconds
  private var simpleValuesHandler: Option[SimpleValuesHandler] = None

  def index(): Action[AnyContent] = Action {
    implicit request => {
      dataStore(AllForKeyKind(KeyKind.portKey)).forAll(fieldEntries =>
        if (simpleValuesHandler.isEmpty)
          simpleValuesHandler = Some(new SimpleValuesHandler(fieldEntries))

        val rows: Seq[Row] = fieldEntries
          .groupBy(_.fieldKey.fieldName)
          .toSeq
          .sortBy(_._1)
          .map { (name, portEntries) =>
            Row(name, portEntries.sortBy(_.fieldKey.key.rc210Value))
          }
        Ok(views.html.ports(rows)))
    }
  }

  def save(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val collect: UpdateData = simpleValuesHandler.get.collect
      dataStore(collect)
      Redirect(routes.PortsController.index())
  }

}

case class Row(name: String, portEntries: Seq[FieldEntry]) {
  def cells: Seq[String] = {
    portEntries.map { fe =>
      val v: FieldValue = fe.value
      v.toHtmlField(fe.fieldKey)
    }
  }
}