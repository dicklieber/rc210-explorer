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
import net.wa9nnn.rc210.ui.TabKind.Fields

trait AbstractTab:
  def toolTip: String = ""

  def entryName: String

  def indexUrl: String

  def tabKind: TabKind

  /**
   * 
   * @param entryName show to users
   * @param indexUrl how
   * @param toolTip
   * @param tabKind
   */
  case class Tab(override val entryName: String,
               override val indexUrl: String,
               override val toolTip: String,
               override val tabKind: TabKind = Fields) extends AbstractTab

object Tabs:
  val rc210Tab: Tab = Tab("RC-210", routes.IOController.listSerialPorts.url, "RC-210 Operations")
  val changes: Tab = Tab("Changes", routes.CommandsController.index.url, "Pending changes that need to be sent to the RC-210.")
  val noTab: Tab = Tab("none", "", "this should never show.")
  val fileUpload: Tab = Tab("Upload", routes.DataStoreController.upload.url, "Upload a saved JSON file.")
  val security: Tab = Tab("Users", routes.UsersController.users().url, "Edit Users")
  val names: Tab = Tab("Names", routes.NamesController.index.url, "User supp;ied names for varous fields.")
  val tabs: Seq[AbstractTab] =
    KeyKind.values :++ Seq(
      rc210Tab,
      changes,
      noTab,
      fileUpload,
      security,
      names
    )

sealed trait TabKind() extends EnumEntry

object TabKind extends PlayEnum[TabKind] {

  override val values: Seq[TabKind] = findValues

  case object Fields extends TabKind

  case object Rc210Io extends TabKind

  case object Settings extends TabKind

}
