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

package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.field.UiRender.UiRender
import net.wa9nnn.rc210.data.named.{NamedManager, NamedSource}

import scala.util.Try

/**
 * Field is a selction of enumerated values
 *
 * @param options
 */
class UiSelect(fixedOptions: Seq[SelectOption]) extends UiInfo {

  override val uiRender: UiRender = UiRender.select
  val fieldExtractor: FieldExtractor = SelectExtractor()
  val validate: String => Try[String] = (s: String) => Try(s)
  override val prompt: String = "-select-"

  override def options()(implicit namedSource: NamedSource): Seq[SelectOption] = {
    fixedOptions
  }

  /*  def displayForId(id:Int):String = options(id).display
    def idForDisplay(display:String):Int = options.indexWhere(_.display == display)*/
}

object UiSelect {
  def apply(simple: String*): UiSelect = {
    val r: Seq[SelectOption] = simple
      .zipWithIndex
      .map { case (string, index) =>
        SelectOption(index, string)
      }
    new UiSelect(r)
  }
}

/**
 * used in an HTML <select> e.g. <option value="@opt.value">@opt.display</option>
 *
 * @param id      internal
 * @param display what user of JSON sees.
 */
case class SelectOption(id: Int, display: String)