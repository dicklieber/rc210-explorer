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

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import net.wa9nnn.rc210.data.ValuesStore.AllDataEnteries
import net.wa9nnn.rc210.data.field.FieldEntry
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class IOController @Inject()(val controllerComponents: ControllerComponents,
                             @Named("values-actor") valuesStore: ActorRef)(implicit ec: ExecutionContext) extends BaseController {
  implicit val timeout: Timeout = 5.seconds

  def downloadJson(): Action[AnyContent] = Action.async {

    (valuesStore ? AllDataEnteries)
      .mapTo[Seq[FieldEntry]]
      .map { entries =>
        val jsObject = JsObject(
          entries
            .sortBy(_.fieldKey.fieldName)
            .map(fieldValue => fieldValue.fieldKey.param -> fieldValue.fieldValue.contents.toJsValue)
        )
        val sJson = Json.prettyPrint(jsObject)

        Ok(sJson).withHeaders(
          "Content-Type" -> "text/json",
          "Content-Disposition" -> s"""attachment; filename="rc210.json""""
        )
      }
  }
}
