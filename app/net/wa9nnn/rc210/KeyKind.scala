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

package net.wa9nnn.rc210

import enumeratum._
import net.wa9nnn.rc210.util.select.{EnumEntryValue, EnumValue}

sealed trait KeyKind(val maxN: Int, val display: Boolean = false) extends EnumEntry

object KeyKind extends PlayEnum[KeyKind] {

  override def values: IndexedSeq[KeyKind] = findValues

  case object logicAlarmKey extends KeyKind(5)

  case object meterKey extends KeyKind(8, false)

  case object meterAlarmKey extends KeyKind(8, true)

  case object dtmfMacroKey extends KeyKind(195, false)

  case object courtesyToneKey extends KeyKind(10)

  case object functionKey extends KeyKind(1005, false)

  case object macroKey extends KeyKind(105)

  case object messageKey extends KeyKind(70) // 40 (in Main) + 30 (in RTC)

  case object commonKey extends KeyKind(1)

  case object portKey extends KeyKind(3)

  case object scheduleKey extends KeyKind(40)

  case object timerKey extends KeyKind(6)

  case object clockKey extends KeyKind(1)

  case object remoteBaseKey extends KeyKind(1)
}
