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
import net.wa9nnn.rc210.ui.Tabs.TabName

object Tabs {

  val noTab:Tab = Tab("none", "", "this should never show.")
  val flowTab: Tab = Tab("Flow", routes.FlowController.flow().url, "How to Macros to things. How this all works.")
  val commonTab: Tab = Tab("Common", routes.CommonEditorController.index().url, "Global settings")
  val ctTab: Tab = Tab("CT", routes.CourtesyToneEditorController.index().url, "Courtesy Tones")
  val clockTab: Tab = Tab("Clock", routes.ClockController.index.url, "Set clock, DST etc.")

  val logicAlarmTab: Tab = Tab("Logic", routes.LogicAlarmEditorController.index().url, "Logic Alarm settings.")
  val metersTab: Tab = Tab("Meters", routes.MeterController.index.url, "Analog Meters and Alarms")
  //
  //  val dtmfTab: Tab = Tab(KeyKind.dtmfMacroKey)
  //

  val macrosTab: Tab = Tab("Macros", routes.MacroNodeController.index().url, "Macro settings.")
  val messagesTab: Tab = Tab("Messages", routes.MessageController.index().url, "Messages.")

  val portsTab: Tab = Tab("Ports", routes.PortsEditorController.index().url, "Port settings")

  val schedulesTab: Tab = Tab("Schedules", routes.ScheduleController.index().url, "Schedule settings.")
  val timersTab: Tab = Tab("Timers", routes.TimerEditorController.index.url, "Timer settings.")
  val rc210Tab: Tab = Tab("RC-210", routes.IOController.listSerialPorts.url, "RC-210 DownloadActor.")
  val fileUpload: Tab = Tab("Upload", routes.DataStoreController.upload.url, "Upload a saved JSON file.")
  val changes: Tab = Tab("Changes", routes.CandidateController.index().url, "Pending changes that need to be sent to the RC-210.")
  val remoteBase: Tab = Tab("Remote Base", controllers.routes.RemoteBaseController.index.url, "Manabge Remote Base radio.")
  def security: Tab = Tab("Users", routes.UsersController.users().url, "Edit Users")

  val tabs: Seq[Tab] = Seq(
    flowTab,
    metersTab,
    logicAlarmTab,
    commonTab,
    clockTab,
    macrosTab,
    messagesTab,
    ctTab,
    portsTab,
    schedulesTab,
    timersTab,
    remoteBase,
    rc210Tab,
    changes,
  ).sortBy(_.name)


  type TabName = String


}

case class Tab(name: TabName, url: String, tooltip: String)


