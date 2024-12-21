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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row, TableSection}
import controllers.routes
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.ui.{FormField, TableSectionButtons}
import net.wa9nnn.rc210.util.{FieldSelect, SelectOption}
import play.api.libs.json.{Format, JsValue, Json}


case class TimeoutTimerResetPoint(value: TotReset = TotReset.AfterCOS) extends FieldValueSimple() :
  override def displayCell: Cell = Cell(value)

  def toCommands(fieldEntry: FieldEntry): Seq[String] = {
    Seq(s"*2122${value.rc210Value}")
  }


  override def tableSection(key: Key): TableSection =
    TableSectionButtons(key,
      Row("TotReset" -> value)
    )

  override def toRow: Row = Row(
    "TimeoutTimerResetPoint",
    value
  )

  override def toEditCell(key: Key): Cell =
    FormField(key, value)

object TimeoutTimerResetPoint extends SimpleExtractor[TimeoutTimerResetPoint] :

  override def update(formFieldValue: String): FieldValueSimple =
    val totValue = TotReset.withName(formFieldValue)
    TimeoutTimerResetPoint(totValue)

  implicit val fmt: Format[TimeoutTimerResetPoint] = Json.format[TimeoutTimerResetPoint]

  def apply(id: Int): TimeoutTimerResetPoint = {
    new TimeoutTimerResetPoint(TotReset.find(id))
  }

  override def extractFromInts(itr: Iterator[Int], field: FieldDefSimple): TimeoutTimerResetPoint = {
    val id = itr.next()
    apply(id)
  }
