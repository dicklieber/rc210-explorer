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

import com.wa9nnn.util.tableui.{Cell, Row}
import net.wa9nnn.rc210.data.courtesy.CourtesyTone.{cell, cellSpan3}
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.key.KeyFactory.CourtesyToneKey
import play.api.libs.json.JsValue

//noinspection ZeroIndexToHead
case class CourtesyTone(override val key: CourtesyToneKey, segments: Seq[Segment]) extends FieldWithFieldKey[CourtesyToneKey] {
  implicit val k = key

  override def toJsValue: JsValue = ???

  override def display: String = ???

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  /**
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   * @param fieldEntry all the metadata.
   * @return html
   */

  /**
   *
   * @param paramValue candidate from form.
   * @return None if value has not changed, otherwise a new [[FieldValue]].
   */
  override def update(paramValue: String): FieldValue = ???

  override val fieldName: String = "CourtesyTone"

  override def toRow()(implicit namedSource: NamedSource): Row = {
    throw new NotImplementedError() //todo
  }

  def rows()(implicit namedSource: NamedSource): Seq[Row] = {
    val name: String = namedSource.get(key).getOrElse("")
    val nameCell: Cell = Cell.rawHtml(views.html.fieldNamedKey(key, name, CtSegmentKey("name", 99)(key)).toString())
      .withRowSpan(3)

    Seq(
      Row(nameCell,
        //top row with delay/duration that span 3 rows.
        cellSpan3(segments(0).delayMs, CtSegmentKey("Delay", 0, "ms")),
        cell(segments(0).durationMs, CtSegmentKey("Duration", 0, "ms")),
        cellSpan3(segments(1).delayMs, CtSegmentKey("Delay", 1, "ms")),
        cell(segments(1).durationMs, CtSegmentKey("Duration", 1, "ms")),

        cellSpan3(segments(2).delayMs, CtSegmentKey("Delay", 2, units = "ms")),
        cell(segments(2).durationMs, CtSegmentKey("Duration", 2, units = "ms")),

        cellSpan3(segments(3).delayMs, CtSegmentKey("Delay", 3, units = "ms")),
        cell(segments(3).durationMs, CtSegmentKey("Duration", 3, units = "ms")),
      )
      ,
      // tone1 row
      Row(
        cell(segments(0).tone1Hz, CtSegmentKey("Tone1", 0, units = "Hz")),
        cell(segments(1).tone1Hz, CtSegmentKey("Tone1", 1, units = "Hz")),
        cell(segments(2).tone1Hz, CtSegmentKey("Tone1", 2, units = "Hz")),
        cell(segments(3).tone1Hz, CtSegmentKey("Tone1", 3, units = "Hz")),
      )
      ,
      // tone2 row
      Row(
        cell(segments(0).tone2Hz, CtSegmentKey("Tone2", 0, units = "Hz")),
        cell(segments(1).tone2Hz, CtSegmentKey("Tone2", 1, units = "Hz")),
        cell(segments(2).tone2Hz, CtSegmentKey("Tone2", 2, units = "Hz")),
        cell(segments(3).tone2Hz, CtSegmentKey("Tone2", 3, units = "Hz")),
      )
    )
  }
}


object CourtesyTone {
  def cell(value: Int, CtSegmentKey: CtSegmentKey): Cell =
    FieldInt(value).toCell(CtSegmentKey)


  def cellSpan3(int: Int, CtSegmentKey: CtSegmentKey): Cell = {
    val r = cell(int, CtSegmentKey)
      .withRowSpan(3)
    r
  }
}

case class Segment(delayMs: Int, durationMs: Int, tone1Hz: Int, tone2Hz: Int)