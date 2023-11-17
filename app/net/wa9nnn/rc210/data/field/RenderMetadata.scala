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

import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.FieldKey

/**
 * Needed to render a [[FieldValue]] in a [[com.wa9nnn.util.tableui.Cell]] or an html string.
 */
trait RenderMetadata {

  def param: String

  def prompt: String = ""

  def units: String = ""

}

/**
 * Helper to quickly create a [[RenderMetdata]]
 */
object RenderMetadata {
  def apply(name: String, prompt: String = "", units: String = "")(implicit key: Key): RenderMetadata = {
    new RMD(name, prompt, units)
  }
}

case class RMD(name: String, override val prompt: String = "", override val units: String = "")(implicit key: Key) extends RenderMetadata {
  override def param: String = FieldKey(name, key).param
}
