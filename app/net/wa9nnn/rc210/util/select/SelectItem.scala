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


/**
 * An item that that can be
 *  - Stored in a case class.
 *  - Displayed in HTML in a selectOptions option
 *  - Used in [p;ay json as [[play.api.libs.json.Format]]
 *  - Play [[play.api.mvc.PathBindable]]
 *
 * Usually managed in a [[SelectItemNumber]]
 */
trait SelectItem:
  /**
   * Shown in selectOptions / option html.
   */
  val display: String = toString

  /**
   *
   * @param formValue as selected by user in form.
   * @return
   */
  def isSelected(formValue: String): Boolean

  //
  //  /**
  //   *
  //   * @param number from RC-210 data
  //   * @return
  //   */
  //  def isSelected(number: Int): Boolean
  //
  //  def item: (String, String)
  def html: String = s""""<option value="$display">$display</option>"""
