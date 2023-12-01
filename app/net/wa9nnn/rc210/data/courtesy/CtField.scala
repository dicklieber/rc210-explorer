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

package net.wa9nnn.rc210.data.courtesy

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.ui.FormField

case class Name()(implicit val key: Key, val segments: Seq[Segment]) extends CtField:
  val value: Int = key.rc210Value
  val name = "name"
  val max = 2000
  val units = ""
  override val segment: Int = -1
  override val rowSpan = 3

case class Delay(segment: Int)(implicit val key: Key, val segments: Seq[Segment]) extends CtField:
  val value: Int = segments(segment).delayMs
  val name = "Delay"
  val max = 2000
  val units = "Ms"
  override val rowSpan = 3

case class Duration(segment: Int)(implicit val key: Key, val segments: Seq[Segment]) extends CtField:
  val value: Int = segments(segment).durationMs
  val name = "Duration"
  val max = 1000
  val units = "Ms"


case class Tone(segment: Int, toneNo: Int)(implicit val key: Key, val segments: Seq[Segment]) extends CtField:
  val value: Int = toneNo match
    case 1 =>
      segments(segment).tone1Hz
    case 2 =>
      segments(segment).tone1Hz
  val name = s"Tone$toneNo"
  val max = 3500
  val units = "Hz"

abstract class CtField extends LazyLogging {
  def body: String =
    FormField(param, value, Option(0 to max))

  val cssClass: String = "ctCell"
  val value: Int

  val name: String
  val segment: Int
  val key: Key
  val max: Int
  val units: String
  val rowSpan: Int = 1

  def param: String = s"$name.$segment"
}



