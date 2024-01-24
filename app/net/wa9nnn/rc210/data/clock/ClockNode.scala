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

package net.wa9nnn.rc210.data.clock

import com.wa9nnn.wa9nnnutil.JsonFormatUtils.javaEnumFormat
import com.wa9nnn.wa9nnnutil.tableui.{Row, Table}
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import net.wa9nnn.rc210.data.clock.MonthOfYearDST.*
import net.wa9nnn.rc210.data.clock.Occurrence.*
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue, FieldEntry, FieldEntryBase, FieldOffset, FieldValue}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.EditButtonCell
import play.api.libs.json.{Format, JsValue, Json}
import play.api.data.*
import play.api.data.Forms.*
import play.api.i18n.MessagesProvider
import play.api.mvc.{RequestHeader, Result}

case class ClockNode(key: Key,
                     enableDST: Boolean = true,
                     hourDST: Int = 2,
                     startDST: DSTPoint = DSTPoint(March, First),
                     endDST: DSTPoint = DSTPoint(November, Second),
                     say24Hours: Boolean = false,
                    ) extends ComplexFieldValue():

  override def displayHtml: String = "todo"

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    //NOTE: To disable DST program the START month to 0, i.e. *21310
    /*
    *2131x yy z where x = 1 is to program the START month and 0 is to program the END month yy is the month (01-12) and must be 2 digits and z Is the occurrence of Sunday in that month (1 – 5).
    For example, in the United States, Daylight Savings time begins the 2nd Sunday in March and ends the 1st Sunday in November: *2131 1 03 2 <----Program START month for March, the 2nd Sunday
    *2131 0 11 1 <----Program END month for November, the 1st Sunday
    Setting the Start and Ending Hour
    You may individually selectOptions the hour at which the DST change starts and ends are made. *2132 x yy where x = 1 for Start Hour and 2 for End Hour and yy = 00 - 23
    For example, in the United States, the Start and End Hours are both 2 AM
    *2132 1 02 <-----Program DST Start Hour to 2 AM
    *2132 1 02 <-----Program DST End Hour to 2 AM

     */
    val startHour = f"1*21321$hourDST%02d"

    val start: String = if (enableDST)
      s"1*21311${startDST.commandPiece}"
    else
      s"1*21310"

    val end = s"1*21311${endDST.commandPiece}"

    val say24 = if (say24Hours)
      s"1*51031"
    else
      s"1*51030"
    Seq(
      startHour,
      start,
      end,
      say24
    )
  }

  override def toJsValue: JsValue = Json.toJson(this)

  override def toRow: Row = Row(
    EditButtonCell(fieldKey),
    key.keyWithName,
    enableDST,
    hourDST,
    startDST,
    endDST,
    say24Hours
  )

object ClockNode extends ComplexExtractor[ClockNode] {
  override val keyKind: KeyKind = KeyKind.Clock
  override val form: Form[ClockNode] = Form(
    mapping(
      "key" -> of[Key],
      "enableDST" -> boolean,
      "hourDST" -> number(min = 0, max = 23),
      "startDST" -> DSTPoint.dstPointForm,
      "endDST" -> DSTPoint.dstPointForm,
      "say24Hours" -> boolean
    )(ClockNode.apply)(ClockNode.unapply))

  def unapply(u: ClockNode): Option[(Key, Boolean, Int, DSTPoint, DSTPoint, Boolean)] = Some((u.key, u.enableDST, u.hourDST, u.startDST, u.endDST, u.say24Hours))

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val startHour: Int = memory(4050)
    val enableDST: Boolean = memory.bool(3687) //DSTFlag - 3687
    val startDST = DSTPoint(memory.stringAt(4042))
    val endDST = DSTPoint(memory.stringAt(4046))
    //    SimpleField(1186, "Clock 24 Hours", commonKey, "n*5103b", FieldBoolean),
    val say24Hours: Boolean = memory.bool(1186)

    val clock = new ClockNode(key, enableDST, startHour, startDST, endDST, say24Hours)
    Seq(
      FieldEntry(this, fieldKey(key), clock)
    )
  }

  /**
   * for various things e.g. parser name.
   */
  override val name: String = "Clock"

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[ClockNode]

  override val fieldName: String = name
  val key: Key = Key(KeyKind.Clock) // there's only one
  val fieldKey: FieldKey = FieldKey(fieldName, key)

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(1186, this, "say24Hours"),
    FieldOffset(3687, this, "DSTFlag"),
    FieldOffset(4042, this, "startDST"),
    FieldOffset(4046, this, "endDST"),
    FieldOffset(4050, this, "hour"),
  )

  implicit val fmtClock: Format[ClockNode] = Json.format[ClockNode]

  override def index(values: Seq[ClockNode]): Table = ???

  override def editOp(form: Form[ClockNode], fieldKey: FieldKey)(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result = ???

  override def bindFromRequest(data: Map[String, Seq[String]]): ComplexFieldValue = ???
}

