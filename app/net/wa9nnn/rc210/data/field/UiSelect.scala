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
import net.wa9nnn.rc210.data.named.NamedSource

import scala.util.Try

class UiSelect(options:Seq[SelectOption]) extends UiInfo{

  override val uiRender: UiRender = UiRender.select
  val fieldExtractor:FieldExtractor = FieldExtractors.int8
  val validate: String => Try[String] = (s: String) => Try(s)
  override val prompt: String = "-select-"

  def options(namedSource: NamedSource): Seq[SelectOption] = {
    options
  }
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

case class SelectOption(value: Int, display: String)