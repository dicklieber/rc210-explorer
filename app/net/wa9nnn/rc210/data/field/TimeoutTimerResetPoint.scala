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

import com.wa9nnn.wa9nnnutil.tableui.{Row, TableSection}
import controllers.routes
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.ui.{FormField, TableSectionButtons}
import net.wa9nnn.rc210.util.{FieldSelect, SelectOption}
import play.api.libs.json.{JsValue, Json}
import views.html.editButton

/**
 * An enumeration with behaviour.
 *
 * @param value    one of the display values in DayOfWeek.options.
 */
case class TimeoutTimerResetPoint(value: TotReset = TotReset.values.head) extends SimpleFieldValue() {
  def displayHtml: String = "TotResetPoint"

  def toCommands(fieldEntry: FieldEntryBase): Seq[String] =
    Seq(s"*2122${value.rc210Value}")

  override def update(paramValue: String): TimeoutTimerResetPoint = {
    TimeoutTimerResetPoint(TotReset.withName(paramValue))
  }

  override def toHtmlField(fieldKey: FieldKey): String = FormField(fieldKey, value)

  override def toJsValue: JsValue = Json.toJson(value)

  override def tableSection(fieldKey: FieldKey): TableSection =
    TableSectionButtons(fieldKey,
      routes.PortsController.index,
      "TotReset" -> value)

  override def toRow: Row = Row(
    "TimeoutTimerResetPoint",
    value
  )
}

object TimeoutTimerResetPoint extends SimpleExtractor {

  def apply(id: Int): TimeoutTimerResetPoint = {
    new TimeoutTimerResetPoint(TotReset.find(id))
  }

  override def extractFromInts(itr: Iterator[Int], field: SimpleField): TimeoutTimerResetPoint = {
    val id = itr.next()
    apply(id)
  }

  override def parse(jsValue: JsValue): FieldValue = new TimeoutTimerResetPoint(jsValue.as[TotReset])

  override val name: String = "TOT Reset Point"

}
