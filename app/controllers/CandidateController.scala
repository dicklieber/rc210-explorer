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

import com.wa9nnn.util.tableui.{Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.DataStore
import play.api.mvc._

import javax.inject.{Inject, Singleton}

@Singleton()
class CandidateController @Inject()(dataStore: DataStore) extends MessagesInjectedController {
  def index(): Action[AnyContent] = Action { implicit request =>
    val candidates = dataStore.candidates
    val rows: Seq[Row] =
      candidates.map { fieldEntry =>
        val candidate = fieldEntry.candidate.get
        Row(fieldEntry.fieldKey.key.toCell, fieldEntry.fieldKey.fieldName,  fieldEntry.fieldValue.display, candidate.display, fieldEntry.toCommand)
      }
    val table = Table(Header(s"Candidates (${candidates.length})", "Key", "Field",  "Was", "New", "Command"), rows)

    Ok(views.html.candidates(table))
  }


}

