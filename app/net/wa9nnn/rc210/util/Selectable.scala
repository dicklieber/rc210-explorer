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
import play.api.data.FormError
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter

import scala.reflect.ClassTag

abstract class Selectable[T <: SelectItem : ClassTag] {
  val choices: Seq[SelectItem]

  def options: Seq[(String, String)] = choices.map(_.item)

  /**
   *
   * @param number RC-210 number
   */
  def lookup(number: Int): T = {
    choices.find(_.isSelected(number)).getOrElse(choices.head).asInstanceOf[T]
  }

  /**
   *
   * @param display user display. Typically from a <form>
   */
  def lookup(display: String): T = {
    val t: T = choices.find(_.isSelected(display)).getOrElse(choices.head).asInstanceOf[T]
    t
  }

  implicit object formatter extends Formatter[T] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
      val r: Either[scala.Seq[FormError], T] = parsing(s => lookup(s), s"Bad value for : $key", Nil)(key, data)
      r
    }
    //      parsing(Offset(_), "error.url", Nil)(ØΩkey, data)


    override def unbind(key: String, t: T): Map[String, String] = {
      Map(key -> t.display)
    }
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
   * @param formValue as seledcted by user in form.
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

}

/**
 * A slelectable item that has a number that goes into an RC-210 command.
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
  def toCell = Cell(display)
}



