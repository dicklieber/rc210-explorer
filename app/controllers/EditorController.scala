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
import net.wa9nnn.rc210.data.ValuesStore
import net.wa9nnn.rc210.data.ValuesStore.ValuesForKey
import net.wa9nnn.rc210.data.field.{FieldEditor, FieldEntry}
import net.wa9nnn.rc210.data.named.{NamedKey, NamedManager}
import net.wa9nnn.rc210.key.KeyKindEnum.{KeyKind, commonKey, macroKey}
import net.wa9nnn.rc210.key.{CommonKey, Key, KeyFormats, KeyKindEnum, Keys}
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

class EditorController @Inject()(val controllerComponents: ControllerComponents,
                                 @Named("values-actor") valuesStore: ActorRef,
                                 namedManager: NamedManager
                                )(implicit ec: ExecutionContext, fieldEditor: FieldEditor)
  extends BaseController {

  implicit val timeout: Timeout = 5.seconds


  def editUnselected(sKeyKind: String): Action[AnyContent] = Action {
    val keyKind: KeyKind = KeyKindEnum.apply(sKeyKind)
    val nk: Seq[NamedKey] = Keys.apply(keyKind).map { key =>
      NamedKey(key, namedManager.get(key).getOrElse(""))
    }
    if (keyKind == KeyKindEnum.commonKey)
      Redirect(routes.EditorController.edit(sKeyKind, CommonKey(1).toString))
    else
      Ok(views.html.editor(keyKind, nk, Seq.empty))

  }

  def edit(sKeyKind: String, sMaybeKey: String): Action[AnyContent] = Action.async {
    val keyKind: KeyKind = KeyKindEnum.apply(sKeyKind)

    val maybeKey: Option[Key] = sMaybeKey match {
      case "" if keyKind == commonKey =>
        Option(CommonKey())
      case "" =>
        // Nothing to edit until user selects a Key.
        None
      case sKey =>
        //Edit key user selected.
        Option(KeyFormats.parseString(sKey))
    }


    val nk: Seq[NamedKey] = Keys.apply(keyKind).map { key =>
      NamedKey(key, namedManager.get(key).getOrElse(""))
    }

    maybeKey match {
      case Some(key) =>
        (valuesStore ? ValuesForKey(key)).mapTo[Seq[FieldEntry]]
          .map { entries: Seq[FieldEntry] =>
            Ok(views.html.editor(keyKind, nk, entries))
          }
      case None => Future(Ok(views.html.editor(keyKind, nk, Seq.empty)))
    }
  }
}