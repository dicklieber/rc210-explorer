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
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.ui.{EditButton, RollbackButton, TabE, Tabs}
import net.wa9nnn.rc210.ui.nav.TabKind
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.mvc.Results.Ok
import play.twirl.api.Html
import views.html.{NavMain, explorer, justdat}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class DataStoreExplorerController @Inject()(dataStore: DataStore, navMain: NavMain)(implicit cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with LazyLogging {

  def filtered(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      val data: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
      val value: String = data("filterKeyKind").head
      val keyKind = KeyKind.withName(value)
      val fieldEntries = dataStore(keyKind)
      val table: Table = process(fieldEntries)
      val html = explorer(table, keyKind)
      Ok(navMain(TabE.Explore, html))
  }

  def index: Action[AnyContent] = Action {
    val table = process(dataStore.all)
    val html = explorer(table, KeyKind.All)
    Ok(navMain(TabE.Explore, html))
  }

  def process(fieldEntries: Seq[FieldEntry]): Table = {
    val rows = fieldEntries
      .sortBy(_.fieldKey.display)
      .map { fieldEntry =>
        Row(
          EditButton.cell(fieldEntry.fieldKey),
          Cell(fieldEntry.fieldKey.display)
            .withToolTip(fieldEntry.fieldKey.toString),
          fieldEntry.fieldValue.displayCell,
          RollbackButton(fieldEntry.fieldKey),
          fieldEntry.candidate.map(_.displayCell).getOrElse("-")
        )
      }
    val header = Header(Seq(
      Seq(Cell(s"DataStore(${rows.length})")
        .withColSpan(5)),
      Seq(
        Cell(""),
        Cell("FieldKey"),
        Cell("Value"),
        Cell("Candidate")
          .withColSpan(2)
      )
    )
    )

    Table(header, rows)
  }
}

