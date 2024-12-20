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
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.ui.{ButtonCell, NamedKeyManager, TabE}
import net.wa9nnn.rc210.ui.TabE.Names
import play.api.mvc.*
import views.html.NavMain

import javax.inject.{Inject, Singleton}
import scala.language.postfixOps

@Singleton()
class NamesController @Inject()(namedKeyManager:NamedKeyManager,
                                navMain: NavMain,                               
                                components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging:
  /**
   * Show all defined names.
   *
   * @return
   */
  def index: Action[AnyContent] = Action {
    implicit request =>
      val rows: Seq[Row] = namedKeyManager.namedKeys.map { namedKey =>
        val key = namedKey.key
        val fieldKey = FieldKey(key)
        val keyKind = key.keyKind
        Row(
          ButtonCell.edit(fieldKey),
          Cell(key),
          namedKey.name
        )
      }
      val header = Header.singleRow(
        "",
        Cell("Key")
          .withToolTip("Keys are, usually, RC-210 data that are numbered."),
        "Name"
      )
      val table = Table(header, rows)

      Ok(navMain(Names, views.html.names(table)))
  }





