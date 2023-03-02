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

import net.wa9nnn.rc210.data.field.UiInfo.checkBox
import net.wa9nnn.rc210.key.KeyKind._

object FieldDefinitions {

  import fieldDefintionSugar.FieldDefintionSugar._

  val fields: Seq[FieldMetadata] = Seq(
    (0, "Site Prefix", miscKey, "*2108V") % UiDtmf(3),
    (4, "TT PadTest", miscKey, "*2093V") % UiDtmf(5),
    (10, "Say Hours", miscKey, "*5104B") % checkBox,
  )

}
