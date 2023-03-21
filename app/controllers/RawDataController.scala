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
import com.wa9nnn.util.tableui.{Header, Table}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.functions.{FunctionNode, FunctionsProvider}
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.data.vocabulary.{Phrase, Vocabulary}
import play.api.mvc._

import javax.inject._
import scala.concurrent.duration.DurationInt

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RawDataController @Inject()(val controllerComponents: ControllerComponents,
                                  functions: FunctionsProvider,
                                  mappedValues: MappedValues)
  extends BaseController {

  implicit val timeout: Timeout = 5.seconds

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def functions(): Action[AnyContent] = Action {
    val schedulesTable = Table(FunctionNode.header(functions.functions.length), functions.functions.map(_.toRow))
    Ok(views.html.dat(Seq(schedulesTable)))
  }

  def mappedItems(): Action[AnyContent] = Action {

    val allEntries = mappedValues.all
    val table: Table = Table(FieldEntry.header(allEntries.length), allEntries.map(_.toRow))
    Ok(views.html.dat(Seq(table)))
  }

  def vocabulary(): Action[AnyContent] = Action {

    val phrases = Vocabulary.phrases.sortBy(_.wordKey)
    val macrosTable = Table(Phrase.header(phrases.length), phrases.map(_.toRow))
    Ok(views.html.dat(Seq(macrosTable)))

  }

  def dumpFields(): Action[AnyContent] = Action {

    val rows = mappedValues.all.map(_.toRow)
    val table = Table(Header(s"All Fields (${rows.length})", "Field Key", "Value", "Candidate"), rows)

    Ok(views.html.dat(Seq(table)))

  }
}
