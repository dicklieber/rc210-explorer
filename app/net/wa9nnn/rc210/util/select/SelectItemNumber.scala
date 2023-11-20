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

package net.wa9nnn.rc210.util.select

import com.wa9nnn.util.tableui.Cell
import enumeratum.{EnumEntry, PlayEnum, PlayFormFieldEnum}
import net.wa9nnn.rc210.util.select.*

trait SelectItemNumber extends SelectItem with EnumEntry with PlayEnum[SelectItemNumber]:
  /**
   * as passed to/from RC-210
   */
  val number: Int
  /**
   * What is shown to user.
   */
  val display: String

  //  override def item: (String, String) = display -> display
  //
  //  override def isSelected(formValue: String): Boolean = display == formValue
  //
  //  /**
  //   *
  //   * @param number from RC-210 data
  //   * @return
  //   */
  //  override def isSelected(number: Int): Boolean = number == this.number

  def toCell: Cell = Cell(display)
