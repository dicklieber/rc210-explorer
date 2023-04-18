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
  val commonTab: Tab = Tab("Common", routes.CommonEditorController.index().url, "Global settings")
  val ctTab: Tab = Tab("CT", routes.CourtesyToneEditorController.index().url, "Courtesy Tones")


  val logicAlarmTab: Tab = Tab("Logic", routes.LogicAlarmEditorController.index().url, "Logic Alarm settings.")
  val metersTab: Tab = Tab("Meters", routes.MeterEditorController.index().url, "Meter Faces")
  //
  //  val dtmfTab: Tab = Tab(KeyKind.dtmfMacroKey)
  //

  val macrosTab: Tab = Tab("Macros", routes.MacroNodeController.index().url, "Macro settings.")
  val messagesTab: Tab = Tab("Messages", routes.MessageController.index().url, "Messages.")

  val portsTab: Tab = Tab("Ports", routes.PortsEditorController.index().url, "Port settings")

  val schedulesTab: Tab = Tab("Schedules", routes.ScheduleController.index().url, "Schedule settings.")
  val timersTab: Tab = Tab("Timers", routes.TimerEditorController.index().url, "Timer settings.")
  val rc210Tab: Tab = Tab("RC-210", routes.IOController.listSerialPorts().url, "RC-210 Download.")
  val fileUpload: Tab = Tab("Upload", routes.DataStoreController.upload.url, "Upload a saved JSON file.")
  val changes: Tab = Tab("Changes", routes.CandidateController.index.url, "Pending changes that need to be sent to the RC-210.")


  val tabs: Seq[Tab] = Seq(
    metersTab,
    logicAlarmTab,
    commonTab,
    macrosTab,
    messagesTab,
    ctTab,
    portsTab,
    schedulesTab,
    timersTab,
    rc210Tab,
    changes,
    //    Tab(KeyKind.dtmfMacroKey),
    //    Tab(KeyKind.functionKey),
    //    Tab(KeyKind.messageMacroKey),
    //    Tab(KeyKind.commonKey),
    //    Tab("Macros", routes.MacroNodeController.index().url),
    //    Tab("Ports", routes.PortsEditorController.index().url),
    //    Tab("Ports", routes.ScheduleController.index().url),
  )


  type TabName = String
}

case class Tab(name: TabName, url: String, tooltip: String)


