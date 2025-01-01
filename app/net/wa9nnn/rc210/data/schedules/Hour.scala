/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */
package net.wa9nnn.rc210.data.schedules

import net.wa9nnn.rc210.data.field.Rc210EmumEntry
import net.wa9nnn.rc210.ui.Rc210Enum

/**
 * Values for the a [[ScheduleNode]] hours.
 */
sealed trait Hour(val rc210Value: Int, maybeName: Option[String] = None) extends Rc210EmumEntry:

  override def entryName: String = maybeName.getOrElse {
    if rc210Value < 12 then
      s"${rc210Value}AM"
    else
      s"${rc210Value - 12}PM"
  }

object Hour extends Rc210Enum[Hour] {

  override val values: IndexedSeq[Hour] = findValues

  case object H0 extends Hour(0, Option("12AM"))

  case object H1 extends Hour(1)

  case object H2 extends Hour(2)

  case object H3 extends Hour(3)

  case object H4 extends Hour(4)

  case object H5 extends Hour(5)

  case object H6 extends Hour(6)

  case object H7 extends Hour(7)

  case object H8 extends Hour(8)

  case object H9 extends Hour(9)

  case object H10 extends Hour(10)

  case object H11 extends Hour(11)

  case object H12 extends Hour(12)

  case object H13 extends Hour(13)

  case object H14 extends Hour(14)

  case object H15 extends Hour(15)

  case object H16 extends Hour(16)

  case object H17 extends Hour(17)

  case object H18 extends Hour(18)

  case object H19 extends Hour(19)

  case object H20 extends Hour(20)

  case object H21 extends Hour(21)

  case object H22 extends Hour(22)

  case object H23 extends Hour(23)

  case object Disabled extends Hour(25, Option("Disabled"))

  case object Every extends Hour(99, Option("Every"))
}
