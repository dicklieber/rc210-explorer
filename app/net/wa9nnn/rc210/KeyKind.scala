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
import net.wa9nnn.rc210.ui.{EnumEntryValue, EnumValue}

sealed trait KeyKind(val maxN: Int) extends EnumEntry:
  def spaces: String =
    entryName.replaceAll(
      String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"
      ),
      " "
    )

object KeyKind extends PlayEnum[KeyKind] {

  override def values: IndexedSeq[KeyKind] = findValues

  case object LogicAlarm extends KeyKind(5)

  case object Meter extends KeyKind(8)

  case object MeterAlarm extends KeyKind(8)

  case object DtmfMacro extends KeyKind(195)

  case object CourtesyTone extends KeyKind(10)

  case object Function extends KeyKind(1005)

  case object RcMacro extends KeyKind(105)

  case object Message extends KeyKind(70) // 40 (in Main) + 30 (in RTC)

  case object Clock extends KeyKind(1)

  case object Port extends KeyKind(3)

  case object Schedule extends KeyKind(40)

  case object Timer extends KeyKind(6)

  case object Common extends KeyKind(1)

  case object RemoteBase extends KeyKind(1)

}
