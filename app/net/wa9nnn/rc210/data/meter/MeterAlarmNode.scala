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

import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.KeyKind.{Macro, Meter, MeterAlarm}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.{ButtonCell, TableSectionButtons}
import net.wa9nnn.rc210.ui.html.meterAlarmEditor
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import views.html.fieldIndex

import java.util.concurrent.atomic.AtomicInteger

case class MeterAlarmNode(val key: Key, meter: Key, alarmType: AlarmType, tripPoint: Int, macroKey: Key) extends ComplexFieldValue(macroKey):
  key.check(KeyKind.MeterAlarm)
  meter.check(Meter)
  macroKey.check(KeyKind.Macro)

  private def rows: Seq[Row] = Seq(
    "Meter" -> meter.keyWithName,
    "Alarm Type" -> alarmType,
    "Trip Point" -> tripPoint,
  ).map(Row(_))

  override def toRow: Row = Row(
    ButtonCell.edit(fieldKey),
    key.keyWithName,
    meter.keyWithName,
    alarmType.entryName,
    tripPoint,
    macroKey.keyWithName
  )

  override def tableSection(fieldKey: FieldKey): TableSection =
    TableSectionButtons(fieldKey, rows: _*)

  override def displayCell: Cell =
    KvTable.inACell(
      "Meter" -> meter.keyWithName,
      "AlarmType" -> alarmType,
      "Trip Point" -> tripPoint,
      "Macro" -> macroKey.keyWithName,
    )

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    /**
     * *2066 alarm number * meterEditor number * alarmtype * trippoint * macro to run *
     * There are 8 meterEditor alarms, 1 through 8
     * Meter Number is 1 through 8 (for the ADC channels) AlarmType determines the action taken by that alarm:
     * 1 - Low Alarm 2 - High Alarm
     */
    val aNumber = key.rc210Value
    val mNumber = meter.rc210Value
    val at = alarmType.rc210Value
    val trip = tripPoint //todo don't we need this?
    val macNumber = macroKey.rc210Value
    Seq(
      s"1*2066$aNumber*$mNumber*$at*$macNumber*"
    )
  }

  override def toJsValue: JsValue = Json.toJson(this)


/*
*2064 C * M* X1* Y1* X2* Y2* C= Channel 1 to 8 M=MeterKind Type 0 to 6 X1, Y1, X2, Y2 represent two calibration points. There must be 6 parameters entered to define a meterEditor face, each value ending with *.
  There are 8 meterEditor faces corresponding to the 8 Analog inputs, with each meterEditor face programmed with 1 of 6 values. The programming command consists the input port, meterEditor face type (name), and 4 values representing:
• The low sensed voltage appearing on an input (X1)
• The low meterEditor face reading (Y1)
• The high sensed voltage appearing on an input (X2)
• The high meterEditor face reading (Y2)

* *2066 alarm number * meterEditor number * alarmtype * trippoint * macro to run *
*
*
1600xy where x = 1 to 8 for the meterEditor channel and y = 1 for ON and 0 for OFF
*
* */

object MeterAlarmNode extends ComplexExtractor[MeterAlarmNode]:
  override val keyKind: KeyKind = KeyKind.MeterAlarm

  def unapply(u: MeterAlarmNode): Option[(Key, Key, AlarmType, Int, Key)] = Some((u.key, u.meter, u.alarmType, u.tripPoint, u.macroKey))

  val form: Form[MeterAlarmNode] = Form(
    mapping(
      "key" -> of[Key],
      "meter" -> of[Key],
      "alarmType" -> AlarmType.formField,
      "tripPoint" -> number,
      "macroKey" -> of[Key]
    )(MeterAlarmNode.apply)(MeterAlarmNode.unapply))

  private val nMeters = 8

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val mai = new AtomicInteger()
    val alarmType: Seq[AlarmType] = memory.sub8(274, nMeters).map(AlarmType.find)
    val macroKeys: Seq[Key] = memory.sub8(314, nMeters).map(Key(Macro, _))
    val setPoint: Seq[String] = memory.chunks(282, 4, nMeters).map { chunk =>
      val array = chunk.ints
      //      new String(array.map(_.toChar))
      "42"
    }
    val meters = memory.sub8(266, nMeters).map { number =>
      try {
        val r: Key = Key(KeyKind.Meter, number + 1)
        r
      } catch {
        case e: Exception =>
          logger.error(s"Bad meterEditor number got: $number , expecting 1 to $nMeters. Will use Meter 1")
          Key(KeyKind.Meter, 1)
      }
    }
    for {i <- 0 until nMeters}
      yield {
        val key: Key = Key(KeyKind.MeterAlarm, mai.incrementAndGet())
        val meterAlarm = MeterAlarmNode(key, meters(i), alarmType(i), setPoint(i).toInt, macroKeys(i))
        new FieldEntry(this, meterAlarm.fieldKey, meterAlarm)
      }
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[MeterAlarmNode]

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(266, this, "Alarm Type"),

    FieldOffset(186, this, "meterFace"),
    FieldOffset(202, this, "meterLowVolt"),
    FieldOffset(218, this, "meterLowrReading"),
    FieldOffset(282, this, "alarm Set Point"),
  )

  implicit val fmtMeterAlarm: Format[MeterAlarmNode] = Json.format[MeterAlarmNode]

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    fieldIndex(keyKind, Table(Header(s"Alarm Meters  (${fieldEntries.length})",
      "",
      "Alarm",
      "Meter",
      "Alarm Type",
      "Trip Point",
      "Macro",
    ),
      fieldEntries.map(_.value.toRow)
    )
    )

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    meterAlarmEditor(form.fill(fieldEntry.value), fieldEntry.fieldKey)

  override def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate] = {
    bind(form.bindFromRequest(data).get)
  }


