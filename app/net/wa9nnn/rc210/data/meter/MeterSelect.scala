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

package net.wa9nnn.rc210.data.meter

import net.wa9nnn.rc210.util.{SelectItemNumber, Selectable}
import play.api.libs.json.{Format, Json}

case class MeterFaceName(value: Int, display: String) extends SelectItemNumber

object MeterFaceNames extends Selectable[MeterFaceName](
  MeterFaceName(0, "Meter OFF"),
  MeterFaceName(1, "Volts"),
  MeterFaceName(2, "Amps"),
  MeterFaceName(3, "Watts"),
  MeterFaceName(4, "Degrees"),
  MeterFaceName(5, "MPH"),
  MeterFaceName(6, "Percent"),
)

case class AlarmType(value: Int, display: String) extends SelectItemNumber

object AlarmType extends Selectable[AlarmType](
  AlarmType(1, "Low Alarm"),
  AlarmType(2, "High Alarm"),
)
