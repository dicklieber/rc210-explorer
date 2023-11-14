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

package net.wa9nnn.rc210.util

import com.wa9nnn.util.tableui.Cell
import net.wa9nnn.rc210.data.remotebase.Mode
import net.wa9nnn.rc210.key.KeyFactory
import play.api.data.FormError
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter

import scala.reflect.ClassTag

/**
 * A collection of [[SelectItem]]s that can be used in a <select> tag.
 *
 * @param choices possible choices.
 * @tparam T
 */
abstract class Selectable[T <: SelectItem](val choices: T*) {
  def options: Seq[(String, String)] = choices.map(_.item)

  /**
   *
   * @param number RC-210 number for this item.
   */
  def lookup(number: Int): SelectItem = {
    choices.find(_.isSelected(number)).getOrElse(choices.head)
  }

  /**
   *
   * @param display user display. Typically from a <form>
   */
  def lookup(display: String): SelectItem = {
    choices.find(_.isSelected(display)).getOrElse(choices.head)
  }
}

/**
 *
 */
trait SelectItem {
  val display: String

  override def toString: String = display

  /**
   *
   * @param formValue as selected by user in form.
   * @return
   */
  def isSelected(formValue: String): Boolean

  /**
   *
   * @param number from RC-210 data
   * @return
   */
  def isSelected(number: Int): Boolean

  def item: (String, String)
  def html:String = s""""<option value="$display">$display</option>"""
}

/**
 * A selectable item that has a number that goes into an RC-210 command.
 */
trait SelectItemNumber extends SelectItem {
  /**
   * as passed to/from RC-210
   */
  val value: Int
  /**
   * What is shown to user.
   */
  val display: String

  override def item: (String, String) = display -> display

  override def isSelected(formValue: String): Boolean = {
    display == formValue
  }

  /**
   *
   * @param number from RC-210 data
   * @return
   */
  override def isSelected(number: Int): Boolean = number == value

  def toCell: Cell = Cell(display)

}



