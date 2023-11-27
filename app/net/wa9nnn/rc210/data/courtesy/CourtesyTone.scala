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

import com.google.common.base.Ascii
import com.typesafe.scalalogging.LazyLogging
//import com.wa9nnn.util.tableui.{Cell, Row}
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.courtesy.CourtesyTone.{cell, cellSpan3}
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntryBase}
import net.wa9nnn.rc210.ui.FormField
import play.api.libs.json.{JsValue, Json, OFormat}

import java.util.concurrent.atomic.AtomicInteger

//noinspection ZeroIndexToHead
case class CourtesyTone(override val key: Key, segments: Seq[Segment]) extends ComplexFieldValue("CourtesyTone") {

  override def display: String = s"$key"

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val segN = new AtomicInteger(1)
    segments.map { segment =>
      segment.toCommand(key.rc210Value, segN.getAndIncrement())
    }
  }

  def rows: Seq[Seq[CtTd]] = {
    val nameCell: CtTd = CtTd(key.rc210Value,CtSegmentKey(key, 99, "name"), 3)

    Seq(
      Seq(nameCell,
        //top row with delay/duration that span 3 rows.
        cellSpan3(segments(0).delayMs, CtSegmentKey(key, 0, "Delay")),
        cell(segments(0).durationMs, CtSegmentKey(key, 0, "Duration")),
        cellSpan3(segments(1).delayMs, CtSegmentKey(key, 1, "Delay")),
        cell(segments(1).durationMs, CtSegmentKey(key, 1, "Duration")),

        cellSpan3(segments(2).delayMs, CtSegmentKey(key, 2, "Delay")),
        cell(segments(2).durationMs, CtSegmentKey(key, 2, "Duration")),

        cellSpan3(segments(3).delayMs, CtSegmentKey(key, 3, "Delay")),
        cell(segments(3).durationMs, CtSegmentKey(key, 3, "Duration")),
      )
      ,
      // tone1 row
      Seq(
        cell(segments(0).tone1Hz, CtSegmentKey(key, 0, "Tone1")),
        cell(segments(1).tone1Hz, CtSegmentKey(key, 1, "Tone1")),
        cell(segments(2).tone1Hz, CtSegmentKey(key, 2, "Tone1")),
        cell(segments(3).tone1Hz, CtSegmentKey(key, 3, "Tone1")),
      )
      ,
      // tone2 row
      Seq(
        cell(segments(0).tone2Hz, CtSegmentKey(key, 0, "Tone2")),
        cell(segments(1).tone2Hz, CtSegmentKey(key, 1, "Tone2")),
        cell(segments(2).tone2Hz, CtSegmentKey(key, 2, "Tone2")),
        cell(segments(3).tone2Hz, CtSegmentKey(key, 3, "Tone2")),
      )
    )
  }

  override def toJsValue: JsValue = Json.toJson(this)
}


object CourtesyTone:
  implicit val fmtSegment: OFormat[Segment] = Json.format[Segment]
  implicit val fmtCourtesyTone: OFormat[CourtesyTone] = Json.format[CourtesyTone]

  def cell(value: Int, ctSegmentKey: CtSegmentKey): CtTd =
    CtTd(value, ctSegmentKey)

  def cellSpan3(int: Int, ctSegmentKey: CtSegmentKey): CtTd = {
    CtTd(int, ctSegmentKey, 3)
  }

case class Segment(delayMs: Int, durationMs: Int, tone1Hz: Int, tone2Hz: Int) {
  def toCommand(number: Int, segN: Int): String = {
    //1*31011200*100*6
    val sNumber = f"$number%02d"

    val spaced = s"1*3$segN$sNumber $delayMs * $durationMs * $tone1Hz * $tone2Hz*"
    spaced.replace(" ", "")
  }
}

object Segment extends LazyLogging {
  def apply(m: Map[String, String]): Segment = {
    logger.trace(s"m: $m")
    try {
      val delay = m("Delay").toInt
      val duration = m("Duration").toInt
      val tone1 = m("Tone1").toInt
      val tone2 = m("Tone2").toInt

      new Segment(delay, duration, tone1, tone2)
    } catch {
      case e: Exception =>
        logger.error("Creating a Segment", e)
        throw e
    }
  }
}