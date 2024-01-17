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
import com.wa9nnn.wa9nnnutil.tableui.{Header, Table}
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import net.wa9nnn.rc210.ui.flow.D3Data

@Singleton
class FlowController @Inject()(dataStore: DataStore)(using components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {

  def flowTable(key: Key): Action[AnyContent] = Action {

    dataStore.flowTable(key).map { fd =>
      val table = fd.table
      Ok(views.html.flow(table))
    }.getOrElse(NotFound(key.keyWithName))
  }
  def flowChart(key: Key): Action[AnyContent] = Action {

    dataStore.flowTable(key).map { fd =>
      Ok(views.html.flowChart(fd))
    }.getOrElse(NotFound(key.keyWithName))
  }

  //  def flow(key: Key): Action[AnyContent] = Action {
  //
  //    dataStore.flow(key).map { fd =>
  //      val table = fd.table(functionsProvider)
  //      Ok(views.html.bubbleSvg())
  //    }.getOrElse(NotFound(key.keyWithName))
  //  }
  def d3Data(key: Key): Action[AnyContent] = Action {
    dataStore.flowTable(key).map { fd =>

      val d3Data: D3Data = fd.d3Data()
      val jsValue: JsValue = Json.toJson(d3Data)
      Ok(jsValue)
    }.getOrElse(NotFound(key.keyWithName))
  }
}