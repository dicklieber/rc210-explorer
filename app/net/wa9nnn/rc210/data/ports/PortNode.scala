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

package net.wa9nnn.rc210.data.ports

import com.wa9nnn.util.tableui.Row
import net.wa9nnn.rc210.PortKey
import net.wa9nnn.rc210.model.Node

import java.time.Period

case class PortNode(key: PortKey,
                    hangTimes: Seq[Int], //HangTime1 - 11-13 //HangTime2 - 14-16 //HangTime3 - 17-19
                    iIdMinutes: Int, //IIDMinutes - 20-22
                    pendingIdMinutes: Int, //PIDMinutes - 23-25
                    txEnble: Boolean, //TxEnable- 26-28
                    dtmfCoverTone: Boolean, //DTMFCovertone - 29-31 1/10 of seconds
                    //DTMFMuteTimer - 32-37

                   ) extends Node {

  override def toRow: Row = throw new NotImplementedError() //todo
}



