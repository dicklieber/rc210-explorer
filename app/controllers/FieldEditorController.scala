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

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.ValuesStore.{SetValue, SetValues, Value, ValuesForKey}
import net.wa9nnn.rc210.data.field.{FieldEditor, FieldEntry}
import net.wa9nnn.rc210.key.{Key, KeyFormats}
import play.api.mvc._
import play.twirl.api.Html

import javax.inject._
import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class FieldEditorController @Inject()(val controllerComponents: ControllerComponents,
                                      @Named("values-actor") valuesStore: ActorRef
                                     )(implicit ec: ExecutionContext, fieldEditor: FieldEditor)
  extends BaseController {

  implicit val timeout: Timeout = 5.seconds

    def selectKey(): Action[AnyContent] = Action {
      val html: Html = views.html.selectKey()
      Ok(html)
    }


  def editFields(sKey: String): Action[AnyContent] = Action.async {
    val key: Key = KeyFormats.parseString(sKey)
    (valuesStore ? ValuesForKey(key)).mapTo[Seq[FieldEntry]].map { fes: Seq[FieldEntry] =>
      Ok(views.html.fieldsEditor(key, fes))
    }
  }

  import play.api.mvc.Action

  def editOne(sKey: String): Action[AnyContent] = Action.async {

      val fieldKey: FieldKey = FieldKey.fromParam(sKey)
      val key: Key = fieldKey.key

      (valuesStore ? Value(fieldKey)).mapTo[Seq[FieldEntry]].map { fes: Seq[FieldEntry] =>
        Ok(views.html.fieldsEditor(key, fes))
      }
  }

  def save(): Action[AnyContent] = Action { request: Request[AnyContent] =>
    val body: AnyContent = request.body
    val formUrlEncoded: Option[Map[String, Seq[String]]] = body.asFormUrlEncoded


    val s: immutable.Iterable[SetValue] = for {
      case (sKey, values) <- formUrlEncoded.get
      value <- values.headOption
    } yield {
      val fieldKey: FieldKey = FieldKey.fromParam(sKey)
      SetValue(fieldKey, value)
    }

    valuesStore ! SetValues(s.toIndexedSeq)
    //    Redirect(routes.FieldEditorController.editFields()
    Ok("//todo")
  }
}