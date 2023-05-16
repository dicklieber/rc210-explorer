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

import com.wa9nnn.util.tableui.{Cell, Row}
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.key.KeyFactory.MeterKey
import net.wa9nnn.rc210.key.KeyFormats._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.{Format, JsValue, Json}

import java.util.concurrent.atomic.AtomicInteger


/**
 * Obe of the 6 Meter channels.
 *
 * @param meterKind face name.
 * @param low       calibrate for high.
 * @param high      calibrate for low.
 */
case class Meter(key: MeterKey, meterKind: MeterFaceName, low: VoltToReading, high: VoltToReading) extends ComplexFieldValue[MeterKey] {
  override val fieldName: String = "Meter"

  override def display: String = toString

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val c = key.number.toString
    val m = meterKind.value
    val x1 = low.hundredthVolt
    val y1 = low.reading
    val x2 = high.hundredthVolt
    val y2 = high.reading

    Seq(
      /*
      *2064 C * M* X1* Y1* X2* Y2* C= Channel 1 to 8 M=Meter Type 0 to 6 X1, Y1, X2, Y2 represent two calibration points.
      * There must be 6 parameters entered to define a meter face, each value ending with *.
       */
      s"1*2064$c*$m*$x1*$y1*$x2*$y2*"
    )
  }

  override def toJsonValue: JsValue = Json.toJson(this)

  override def toRow: Row = throw new NotImplementedError() //todo}
}

object Meter extends ComplexExtractor[MeterKey] {

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */


  def extract(memory: Memory): Seq[FieldEntry] = {
    val mai = new AtomicInteger()
    val nMeters = KeyKind.meterKey.maxN()
    val faceInts = memory.sub8(186, nMeters)
    val faceNames: Array[MeterFaceName] = faceInts.map(MeterFaceName.lookup)
    val lowX: Seq[Int] = memory.iterator16At(202).take(nMeters).toSeq
    val lowY: Seq[Int] = memory.iterator16At(218).take(nMeters).toSeq
    val highX: Seq[Int] = memory.iterator16At(234).take(nMeters).toSeq
    val highY: Seq[Int] = memory.iterator16At(250).take(nMeters).toSeq

    val meters: Seq[FieldEntry] = for {
      i <- 0 until nMeters
    } yield {
      val meterKey = KeyFactory.meterKey(mai.incrementAndGet())
      val low = VoltToReading(lowX(i), lowY(i))
      val high = VoltToReading(highX(i), highY(i))
      val meter: Meter = Meter(meterKey, faceNames(i), low, high)
      new FieldEntry(this, meter.fieldKey, meter)
    }
    meters
  }

  /**
   * for various things e.g. parser name.
   */
  override val name: String = "Meter"

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[Meter]

  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.meterKey

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(186, this, "meterFace"),
    FieldOffset(202, this, "meterLowVolt"),
    FieldOffset(218, this, "meterLowrReading"),
    FieldOffset(282, this, "alarm Set Point"),
    FieldOffset(2064, this, "meter"),
  )

  implicit val fmtVoltToReading: Format[VoltToReading] = Json.format[VoltToReading]
  implicit val fmtMeter: Format[Meter] = Json.format[Meter]
}


/**
 *
 * @param hundredthVolt  input voltaqe
 * @param reading        what "shows" on the4 meter face.
 */
case class VoltToReading(hundredthVolt: Int, reading: Int) {
  def cells: Seq[Cell] = Seq(
    Cell(hundredthVolt),
    Cell(reading)
  )
}