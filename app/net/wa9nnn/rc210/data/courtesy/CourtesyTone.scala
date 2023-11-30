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
import net.wa9nnn.rc210.KeyKind
import play.api.libs.json.Format

import scala.collection.MapView
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntryBase}
import play.api.libs.json.{JsValue, Json, OFormat}

import java.util.concurrent.atomic.AtomicInteger

case class CourtesyTone(override val key: Key, segments: Seq[Segment]) extends ComplexFieldValue("CourtesyTone") {
  assert(segments.length == 4, s"Must have four segemnts but only have: $segments")

  override def display: String = s"$key"

  implicit val k: Key = key
  implicit val s: Seq[Segment] = segments

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val segN = new AtomicInteger(1)
    segments.map { segment =>
      segment.toCommand(key.rc210Value, segN.getAndIncrement())
    }
  }

  def rows: Seq[Seq[CtField]] = {

    Seq( // <tr>
      Seq( // <td>
        Name(),
        //top row with delay/duration that span 3 rows.
        Delay(0),
        Duration(0),
        Delay(1),
        Duration(1),
        Delay(2),
        Duration(2),
        Delay(3),
        Duration(3)
      ),

      Seq(
        Tone(0, 1),
        Tone(1, 1),
        Tone(2, 1),
        Tone(3, 1),
      ),
      // tone2 row
      Seq(
        Tone(0, 2),
        Tone(1, 2),
        Tone(2, 2),
        Tone(3, 2),
      )
    )
  }

  override def toJsValue: JsValue = Json.toJson(this)
}


object CourtesyTone extends LazyLogging:
  def unapply(u: CourtesyTone): Option[(Key, Seq[Segment])] = Some((u.key, u.segments))

  implicit val fmtCourtesyTone: Format[CourtesyTone] = Json.format[CourtesyTone]


