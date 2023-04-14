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

import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.serial.{ComPort, ERamCollector, RC210Data, RC210Download}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataStoreController @Inject()(implicit val controllerComponents: ControllerComponents,
                                    dataStore: DataStore,
                                    datFile: DatFile,
                                    executionContext: ExecutionContext
                            ) extends BaseController with LazyLogging {
  implicit val timeout: Timeout = 5.seconds

  def downloadJson(): Action[AnyContent] = Action {

    val jsObject = Json.toJson(dataStore)
    val sJson = Json.prettyPrint(jsObject)

    Ok(sJson).withHeaders(
      "Content-Type" -> "text/json",
      "Content-Disposition" -> s"""attachment; filename="rc210.json""""
    )
  }


}