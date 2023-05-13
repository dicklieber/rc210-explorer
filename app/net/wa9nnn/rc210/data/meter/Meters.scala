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

import com.wa9nnn.util.tableui.Row
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, MeterKey}
import net.wa9nnn.rc210.key.KeyFormats._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.{Format, JsValue, Json}

case class Meters(referenceVoltage: Int, channels: Seq[Meter], alarms: Seq[MeterAlarm]) extends ComplexFieldValue[MeterKey] {
  override def display: String = {

    throw new NotImplementedError() //todo
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {

    Seq("todo")
  }

  /**
   * Render as HTML. Either a single rc2input of an entire HTML Form.
   *
   * @return html
   */
  override def toHtmlField(renderMetadata: RenderMetadata): String = {

    throw new NotImplementedError() //todo
  }

  override def toJsonValue: JsValue = Json.toJson(this)

  override val key: MeterKey = KeyFactory.meterKey()
  override val fieldName: String = "Meter"

  override def toRow: Row = throw new NotImplementedError() //todo
}

/**
 * Obe of the 6 Meter channels.
 *
 * @param meterKind
 * @param high calibrate for low.
 * @param low  calibrate for high.
 */
case class Meter(name:String, meterKind: MeterFaceName, high: VoltToReading, low: VoltToReading)

case class MeterAlarm(name: String, meterNumber: Int, alarmType: AlarmType, tripPoint: Int, macroKey: MacroKey)

/*
*2064 C * M* X1* Y1* X2* Y2* C= Channel 1 to 8 M=MeterKind Type 0 to 6 X1, Y1, X2, Y2 represent two calibration points. There must be 6 parameters entered to define a meter face, each value ending with *.
  There are 8 meter faces corresponding to the 8 Analog inputs, with each meter face programmed with 1 of 6 values. The programming command consists the input port, meter face type (name), and 4 values representing:
• The low sensed voltage appearing on an input (X1)
• The low meter face reading (Y1)
• The high sensed voltage appearing on an input (X2)
• The high meter face reading (Y2)

* *2066 alarm number * meter number * alarmtype * trippoint * macro to run *
*
*
1600xy where x = 1 to 8 for the meter channel and y = 1 for ON and 0 for OFF
*
* */


object Meters extends ComplexExtractor[MeterKey] {
  private val nMeters = 8

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val vref = memory.iterator16At(186).next()
    val meters = Meters(vref, extractMeters(memory), meterAlarms(memory))
    val meterKey = KeyFactory.meterKey()
    Seq(
      FieldEntry(this, fieldKey(meterKey), meters)
    )
  }

  private def meterAlarms(memory: Memory): Seq[MeterAlarm] = {
    val meterAlarm = memory.sub8(266, nMeters)
    val alarmType: Array[AlarmType] = memory.sub8(274, nMeters).map(AlarmType.lookup)
    val setPoint: Seq[String] = memory.chunks(282, 4, nMeters).map { chunk =>
      val array = chunk.array
      //      new String(array.map(_.toChar))
      "42"
    }
    val macroKeys: Seq[MacroKey] = memory.sub8(314, nMeters).map(KeyFactory.macroKey)
    for {i <- 0 until nMeters}
      yield {
        MeterAlarm("", meterAlarm(i), alarmType(i), setPoint(i).toInt, KeyFactory.macroKey(i + 1))
      }
  }

  private def extractMeters(memory: Memory): Seq[Meter] = {
    val faceNames: Array[MeterFaceName] = memory.sub8(186, nMeters).map(MeterFaceName.lookup)
    val lowX: Seq[Int] = memory.iterator16At(202).take(nMeters).toSeq
    val lowY: Seq[Int] = memory.iterator16At(218).take(nMeters).toSeq
    val highX: Seq[Int] = memory.iterator16At(234).take(nMeters).toSeq
    val highY: Seq[Int] = memory.iterator16At(250).take(nMeters).toSeq

    val meters: Seq[Meter] = for {
      i <- 0 until nMeters
    } yield {
      val low = VoltToReading(lowX(i), lowY(i))
      val high = VoltToReading(highX(i), highY(i))
      Meter("", faceNames(i), low, high)
    }
    meters
  }

  /**
   * for various things e.g. parser name.
   */
  override val name: String = "Meter"

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[Meters]

  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.meterKey

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(184, this, "vRef"), //VREF - 184-185
    FieldOffset(186, this, "meterFace"),
    FieldOffset(202, this, "meterLowVolt"),
    FieldOffset(218, this, "meterLowrReading"),
    FieldOffset(282, this, "alarm Set Point"),
    FieldOffset(2064, this, "meter"),
    FieldOffset(2066, this, "alarm")
  )

  implicit val fmtVoltToReading: Format[VoltToReading] = Json.format[VoltToReading]
  implicit val fmtMeter: Format[Meter] = Json.format[Meter]
  implicit val fmtMeterAlarm: Format[MeterAlarm] = Json.format[MeterAlarm]
  implicit val fmtMeters: Format[Meters] = Json.format[Meters]
}


/**
 *
 * @param hundredthVolt input voltaqge
 * @param reding        what "shows" on the4 meter face.
 */
case class VoltToReading(hundredthVolt: Int, reading: Int)