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

package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.PortKey

class DataStore {
  val ports = new MappedValuesSeq("Ports", 3)
  val schedules = new MappedValuesSeq("Schedules", 40)
  val nonSeqedFields = new MappedValues(PortKey(2))

  def setup(parsedValue: ParsedValue, fieldMetadata: FieldMetadata): Unit = {
/*
    if (parsedValue.fieldKey.key.isDefined) {

    } else
      nonSeqedFields.setupField(parsedValue, fieldMetadata)
*/
  }
}
