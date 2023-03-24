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

//  val alarmtab: Tab = Tab(KeyKind.alarmKey)
//
//  val dtmfTab: Tab = Tab(KeyKind.dtmfMacroKey)
//
//  val commonKeyTab: Tab = Tab(KeyKind.commonKey)

  val macrosTab: Tab = Tab("Macros", routes.MacroNodeController.index().url)

  val portsTab: Tab = Tab("Ports", routes.PortsEditorController.index().url)

  val schedulesTab: Tab = Tab("Schedules", routes.ScheduleController.index().url)


  val tabs: Seq[Tab] = Seq(
//    alarmtab,
//    dtmfTab,
//    commonKeyTab,
    macrosTab,
    portsTab,
    schedulesTab
    //    Tab(KeyKind.alarmKey),
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

case class Tab(name: TabName, url: String)


