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

import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, MeterAlarmKey, MeterKey}
import net.wa9nnn.rc210.key.KeyFormats._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.{Format, JsValue, Json}

import java.util.concurrent.atomic.AtomicInteger


case class MeterAlarm(val key: MeterAlarmKey, meter: MeterKey, alarmType: AlarmType, tripPoint: Int, macroKey: MacroKey) extends ComplexFieldValue[MeterAlarmKey] {
  override val fieldName: String = "MeterAlarm"

  override def display: String = toString

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    /**
     * *2066 alarm number * meter number * alarmtype * trippoint * macro to run *
     * There are 8 meter alarms, 1 through 8
     * Meter Number is 1 through 8 (for the ADC channels) AlarmType determines the action taken by that alarm:
     * 1 - Low Alarm 2 - High Alarm
     */
    val aNumber = key.number
    val mNumber = meter.number
    val at = alarmType.value
    val trip = tripPoint
    val macNumber = macroKey.number
    Seq(
      s"1*2066$aNumber*$mNumber*$at*$macNumber*"
    )
  }

  override def toJsonValue: JsValue = Json.toJson(this)
}

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


object MeterAlarm extends ComplexExtractor[MeterKey] {
  private val nMeters = 8

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val mai = new AtomicInteger()
    val alarmType: Seq[AlarmType] = memory.sub8(274, nMeters).map(AlarmType.lookup)
    val macroKeys: Seq[MacroKey] = memory.sub8(314, nMeters).map(KeyFactory.macroKey)
    val setPoint: Seq[String] = memory.chunks(282, 4, nMeters).map { chunk =>
      val array = chunk.ints
      //      new String(array.map(_.toChar))
      "42"
    }
    val meters = memory.sub8(266, nMeters).map { number =>
      try {
        val r: MeterKey = KeyFactory.meterKey(number)
        r
      } catch {
        case e:Exception =>
          logger.error(s"Bad meter number got: $number , expecting 1 to $nMeters. Will use Meter 1")
          KeyFactory.meterKey(1)
      }
    }
    for {i <- 0 until nMeters}
      yield {
        val key = KeyFactory.meterAlarmKey(mai.incrementAndGet())
        val meterAlarm = MeterAlarm(key, meters(i), alarmType(i), setPoint(i).toInt, macroKeys(i))
        new FieldEntry(this, meterAlarm.fieldKey, meterAlarm)
      }
  }

  /**
   * for various things e.g. parser name.
   */
  override val name: String = "Meter"

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[MeterAlarm]

  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.meterKey

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(266, this, "Alarm Type"),

    FieldOffset(186, this, "meterFace"),
    FieldOffset(202, this, "meterLowVolt"),
    FieldOffset(218, this, "meterLowrReading"),
    FieldOffset(282, this, "alarm Set Point"),
  )

  implicit val fmtMeterAlarm: Format[MeterAlarm] = Json.format[MeterAlarm]
}


/**
 *
 * @param hundredthVolt input voltaqge
 * @param reding        what "shows" on the4 meter face.
 */