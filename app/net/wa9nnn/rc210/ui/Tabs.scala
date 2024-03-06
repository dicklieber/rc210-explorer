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
import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.ui.nav.TabKind
import net.wa9nnn.rc210.ui.nav.TabKind.*
import org.apache.pekko.actor.typed.SupervisorStrategy.restart

/**
 * A [[Tab]] is something shown in the left side navigation menu. 
 * Tab instances are either a [[KeyKind]] that is a member of a [[Key]] or a [[TabE]] which is
 * an enum of individually defined [[Tabs]].
 */
trait Tab:
  def toolTip: String = ""

  def entryName: String

  def indexUrl: String

  def tabKind: TabKind = TabKind.Fields

enum TabE(override val entryName: String,
          override val indexUrl: String,
          override val toolTip: String,
          override val tabKind: TabKind = Fields) extends Tab:
  case SetClock extends TabE("Set Clock", routes.Rc210Controller.setClock().url, "Set RC210 time from server clock.", Rc210Io)

  case Restart extends TabE("Restart", routes.Rc210Controller.restart().url, "Restart RC210 controller.", Rc210Io)
  case SerialPort extends TabE("Serial Port", routes.IOController.listSerialPorts.url, "Configure serial port.", Rc210Io)
  case RC210Download extends TabE("Download", routes.DownloadController.index.url, "Download from RC-210", Rc210Io)
  case Explore extends TabE("Explore", routes.DataStoreExplorerController.index.url, "View the DataStore", Debug)
  case Memory extends TabE("Memory", routes.MemoryController.index.url,  "View raw data received from the RC-210 controller.", Debug)
  case ViewJson extends TabE("Json", routes.DataStoreController.viewJson.url, "View data as JSON.", Debug)
  case Changes extends TabE("Changes", routes.CandidatesController.index.url, "Pending changes that need to be sent to the RC-210.", Rc210Io)
  case FileUpload extends TabE("Upload", routes.DataStoreController.upload().url, "Upload a saved JSON file.", Disk)
  case JsonDownload extends TabE("Save", routes.DataStoreController.downloadJson.url, "Save RC210 data in JSON.", Disk)
  case UserManager extends TabE("Users", routes.UsersController.users().url, "Edit Users", Settings)
  case Names extends TabE("Names", routes.NamesController.index.url, "User supplied names for varous fields.")
  case Rollback extends TabE("Rollback", routes.DataStoreController.rollback().url, "Remove all candidates.", Debug)
  case Logout extends TabE("Logout", routes.LoginController.logout().url, "Finish this session", Settings)

object Tabs:

  val tabs: Seq[Tab] =
    KeyKind
      .values
      .filterNot(_ == KeyKind.Function)
      .appendedAll(TabE.values)
      .sortBy(_.entryName)

  def releventTabs(tabKind: TabKind): Seq[Tab] = {
    tabs
      .filter(_.tabKind == tabKind)
  }
  val noTab = new Tab:
    def entryName = "none"

    def indexUrl = null


