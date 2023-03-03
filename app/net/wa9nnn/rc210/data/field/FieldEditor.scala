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

import net.wa9nnn.rc210.data.named.NamedManager
import play.twirl.api.Html

import javax.inject.{Inject, Singleton}

@Singleton
class FieldEditor @Inject()(implicit namedManager: NamedManager) {
  def apply(fieldEntry: FieldEntry): String = {
    val str1 = fieldEntry.fieldMetadata.uiInfo.uiRender match {
      case net.wa9nnn.rc210.data.field.UiRender.checkbox => {
        val f: Html = views.html.fieldCheckbox(fieldEntry)
        f.toString()
      }
      case net.wa9nnn.rc210.data.field.UiRender.number => {
        views.html.fieldNumber(fieldEntry).toString()
      }
      case net.wa9nnn.rc210.data.field.UiRender.select => {
        val appendable = views.html.fieldSelect(fieldEntry)
        val str = appendable.toString()
        str
      }
      case net.wa9nnn.rc210.data.field.UiRender.dtmfKeys => {
        "dtmf"
      }
      case net.wa9nnn.rc210.data.field.UiRender.twoStrings => {
        "2strings"
      }
    }
    str1
  }
}
