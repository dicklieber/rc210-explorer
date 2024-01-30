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

package net.wa9nnn.rc210.ui

import controllers.routes
import enumeratum.{EnumEntry, PlayEnum}
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.ui.TabKind.{Fields, Rc210Io}

trait Tab:
  def toolTip: String = ""

  def entryName: String

  def indexUrl: String

  def tabKind: TabKind = TabKind.Fields

object Tab:
  def apply(entryName: String, indexUrl: String, tabKind: TabKind, toolTip: String = ""):Tab =
    Tabx(entryName, indexUrl, toolTip, tabKind)
end Tab

/**
 *
 * @param entryName show to users
 * @param indexUrl  how
 * @param toolTip
 * @param tabKind
 */
case class Tabx(override val entryName: String,
                override val indexUrl: String,
                override val toolTip: String,
                override val tabKind: TabKind = Fields) extends Tab

object Tabs:
  val rc210Tab: Tabx = Tabx("RC-210", routes.IOController.listSerialPorts.url, "RC-210 Operations", tabKind = Rc210Io)
  val changes: Tabx = Tabx("Changes", routes.CommandsController.index.url, "Pending changes that need to be sent to the RC-210.", tabKind = Rc210Io)
  val noTab: Tabx = Tabx("none", "", "this should never show.")
  val fileUpload: Tabx = Tabx("Upload", routes.DataStoreController.upload.url, "Upload a saved JSON file.")
  val security: Tabx = Tabx("Users", routes.UsersController.users().url, "Edit Users")
  val names: Tabx = Tabx("Names", routes.NamesController.index.url, "User supp;ied names for varous fields.")
  val tabs: Seq[Tab] =
    KeyKind.values.sortBy(_.entryName) :++ Seq(
      rc210Tab,
      changes,
      noTab,
      fileUpload,
      security,
      names
    )

  def releventTabs(tab:Tab): Seq[Tab] = {
    val desired = tab.tabKind
    tabs.filter(_.tabKind == desired)
  }

sealed trait TabKind(val iconName:String) extends EnumEntry

object TabKind extends PlayEnum[TabKind] {

  override val values = findValues

  case object Fields extends TabKind("bi-database")

  case object Rc210Io extends TabKind("bi-arrow-down-up")

  case object Settings extends TabKind("bi-gear-wide-connected")

}
