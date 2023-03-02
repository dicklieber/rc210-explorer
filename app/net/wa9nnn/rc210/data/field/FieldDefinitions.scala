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
    (0, "Site Prefix", miscKey, "*2108v") % UiDtmf(3),
    (4, "TT PadTest", miscKey, "*2093v") % UiDtmf(5),
    (10, "Say Hours", miscKey, "*5104b") % checkBox,
    (11, "Hang Time 1", portKey, "n*10001v"),
    (14, "Hang Time 2", portKey, "n*10002v"),
    (17, "Hang Time 3", portKey, "n*10003v"),
    (20, "Initial ID Timer  ", portKey, "n*1000v") % UiNumber(255, "minutes"),
    (23, "Pending ID Timer  ", portKey, "n*1003v") % UiNumber(255, "minutes"),
    (26, "Tx Enable", portKey, "n11b") % checkBox,
    (29, "DTMF Cover Tone", portKey, "n13b") % checkBox,
    (32, "DTMF Mute Timer", portKey, "n*1006v") % UiNumber(999, "100 milliseconds"),
    (38, "Kerchunk", portKey, "n15b") % checkBox,
    (41, "Kerchunk Timer", portKey, "n1018b") % UiNumber(6000, "milliseconds"),
    (47, "Mute Digit Select", miscKey, "*2090v") % SelectOptions.dtmfMuteDigit,
  )

}
