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

import com.google.inject.Inject
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind, MacroKey}

import javax.inject.Singleton

@Singleton
class SelectOptions @Inject()(implicit namedManager: NamedManager) {
  val dtmfMuteDigit: UiSelect = new UiSelect(
    Seq(SelectOption(1, "1st digit"),
      SelectOption(2, "2ndt digit"))
  )
  val radioType: UiSelect = new UiSelect(
    Seq(
      SelectOption(1, "Kenwood"),
      SelectOption(2, "Icom"),
      SelectOption(3, "Yaesu"),
      SelectOption(4, "Kenwood V7a"),
      SelectOption(5, "Doug Hall RBI - 1"),
      SelectOption(7, "Kenwood g707"),
      SelectOption(8, "Kenwood 271 A"),
      SelectOption(9, "Kenwood V71a")
    )
  )
  val yaesuType: UiSelect = new UiSelect(
    Seq(
      SelectOption(1, "FT-100D"),
      SelectOption(2, "FT817, FT-857, FT-897"),
      SelectOption(3, "FT847"),
    )
  )


  val macroSelect: UiSelect = new UiSelect(Seq.empty) {

    override val fieldExtractor = SelectExtractor()

    override def options() = {

      KeyFactory.apply(KeyKind.macroKey).map{mk =>
        SelectOption(mk.toString, namedManager(mk))
      }
    }
  }

  SelectOptions.selectOptions = this
}

object SelectOptions {
  var selectOptions: SelectOptions = _
}

case class SelectOption(id:String, display: String, selected: Boolean = false) {
  def select: SelectOption = copy(selected = true)

  def html: String = {
    val s: String = if (selected) " selected " else " "
    s"""<option value="$id" $s >$display</option>"""
  }
}

object SelectOption {
  def apply(id:Int, display: String) :SelectOption = new SelectOption(id.toString, display)

}
