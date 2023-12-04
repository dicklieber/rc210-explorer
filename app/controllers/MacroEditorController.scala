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
import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.data.Dtmf.Dtmf
import net.wa9nnn.rc210.data.datastore.DataStoreActor.{AllForKeyKind, ForFieldKey}
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.macros.RcMacro
import net.wa9nnn.rc210.data.macros.RcMacro.*
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import views.html.macroNodes

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Try
import scala.util.matching.Regex

@Singleton()
class MacroEditorController @Inject()(actor: ActorRef[DataStoreActor.Message])
                                     (implicit scheduler: Scheduler,
                                      functionsProvider: FunctionsProvider,
                                      ec: ExecutionContext, components: MessagesControllerComponents)
  extends MessagesAbstractController(components)
    with LazyLogging {


  implicit val timeout: Timeout = 3 seconds


  def index(): Action[AnyContent] = Action.async { implicit request =>
    val future: Future[Seq[FieldEntry]] = actor.ask(AllForKeyKind(KeyKind.macroKey, _))
    future.map { (fe: Seq[FieldEntry]) =>
      val nodes: Seq[RcMacro] = fe.map(_.value.asInstanceOf[RcMacro])
      Ok(macroNodes(nodes))
    }
  }

  def edit(key: Key): Action[AnyContent] = Action.async { implicit request =>

    val fieldKey = FieldKey("Macro", key)
    actor.ask(ForFieldKey(fieldKey, _)).map {
      case Some(fe: FieldEntry) =>
        Ok(views.html.macroEditor(fe.value))
      case None =>
        NotFound(s"No keyField: $fieldKey")
    }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    val formData: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

    val sKey: String = formData("key").head
    val key: Key = Key.apply(sKey)
    val dtmf = (for {
      d: Dtmf <- formData("dtmf")
      if d.nonEmpty
    } yield {
      d
    }).headOption
    val functions: Seq[Key] = formData("ids")
      .head
      .split(",").toIndexedSeq
      .flatMap { sfunction =>
        Try {
          val f: Key = Key(sfunction)
          f
        }.toOption
      }

    val macroNode = RcMacro(key, functions, dtmf)
    val ud = UpdateCandidate(macroNode.fieldKey, Right(macroNode))

    val keyNames = Seq(NamedKey(key, formData("name").head))
    actor.ask[String](DataStoreActor.UpdateData(Seq(UpdateCandidate(macroNode)), keyNames,
      user(request), _)).map { _ =>
      Redirect(routes.MacroEditorController.index())
    }
  }
}

object MacroEditorController {
  val r: Regex = """[^\d]*(\d*)""".r
}


