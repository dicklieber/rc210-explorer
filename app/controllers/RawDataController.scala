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
import com.wa9nnn.util.tableui.Table
import net.wa9nnn.rc210.DataProvider
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.data.ValuesStore.AllDataEnteries
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.functions.{FunctionNode, FunctionsProvider}
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.data.vocabulary.{MessageMacroNode, Phrase, Vocabulary}
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RawDataController @Inject()(val controllerComponents: ControllerComponents,
                                  functions: FunctionsProvider,
                                  dataProvider: DataProvider,
                                  @Named("values-actor") valuesActor: ActorRef) (implicit ec: ExecutionContext)
  extends BaseController {

  implicit val timeout: Timeout = 5.seconds

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def functions(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val schedulesTable = Table(FunctionNode.header(functions.functions.length), functions.functions.map(_.toRow))
    Ok(views.html.dat(Seq(schedulesTable)))
  }




  def mappedItems(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>

    (valuesActor ? AllDataEnteries).mapTo[Seq[FieldEntry]].map { allEntries: Seq[FieldEntry] =>
      val table: Table = Table(FieldEntry.header(allEntries.length), allEntries.map(_.toRow))
      Ok(views.html.dat(Seq(table)))
    }
  }


  def macros(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val rc210Data: Rc210Data = dataProvider.rc210Data
    val macros = rc210Data.macros
    val macrosTable = Table(MacroNode.header(macros.length), macros.map(_.toRow))
    Ok(views.html.dat(Seq(macrosTable)))
  }


  def schedules(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val rc210Data: Rc210Data = dataProvider.rc210Data
    val schedules = rc210Data.schedules
    val macrosTable = Table(Schedule.header(schedules.length), schedules.map(_.toRow))
    Ok(views.html.dat(Seq(macrosTable)))
  }

  def messageMacros(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val messageMacros: Seq[MessageMacroNode] = dataProvider.rc210Data.messageMacros
    val macrosTable = Table(MessageMacroNode.header(messageMacros.length), messageMacros.map(_.toRow))
    Ok(views.html.dat(Seq(macrosTable)))
  }

  def vocabulary(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val phrases = Vocabulary.phrases.sortBy(_.wordKey)
    val macrosTable = Table(Phrase.header(phrases.length), phrases.map(_.toRow))
    Ok(views.html.dat(Seq(macrosTable)))
  }

}