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
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.data.datastore.{DataStore, DataTransferJson}
import net.wa9nnn.rc210.ui.{EditButtonCell, TabE, Tabs}
import net.wa9nnn.rc210.ui.nav.TabKind
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.*
import views.html.{NavMain, justdat}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class DataStoreExplorerController @Inject()(dataStore: DataStore, navMain: NavMain)(implicit cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {

  def index: Action[AnyContent] = Action {
    val all = dataStore.all
    val rows = all.map { fieldEntry =>
      Row(
        EditButtonCell(fieldEntry.fieldKey),
        Cell(fieldEntry.fieldKey.display)
          .withToolTip(fieldEntry.fieldKey.toString),
        fieldEntry.fieldValue.displayCell,
        fieldEntry.candidate.map(_.displayCell).getOrElse("-")
      )
    }

    val table = Table(Header(s"DataStore(${rows.length})","", "FieldKey", "Value", "Candidate"), rows)
    Ok(navMain(TabE.Explore, justdat(Seq(table))))
  }

}