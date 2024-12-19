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

import com.wa9nnn.wa9nnnutil.tableui.{Cell, KvTable, Row, Table}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.data.meter.MeterAlarmNode.bind
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.ButtonCell
import net.wa9nnn.rc210.ui.html.meterEditor
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.data.Forms.*
import play.api.data.{Form, Mapping}
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.*
import play.twirl.api.Html
import views.html.fieldIndex

import java.util.concurrent.atomic.AtomicInteger

/**
 * Obe of the 6 Meter channels.
 *
 * @param meterFaceName face name.
 * @param low           calibrate for high.
 * @param high          calibrate for low.
 */
case class MeterNode(key: Key, meterFaceName: MeterFaceName, low: VoltToReading, high: VoltToReading) extends ComplexFieldValue():

  override def displayCell: Cell =
    KvTable.inACell(
      "Key" -> key.keyWithName,
      "Face" -> meterFaceName,
      "Low" -> low,
      "High" -> high,
    )

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val c = key.rc210Value.toString
    val m = meterFaceName.toString
    val x1 = low.hundredthVolt
    val y1 = low.reading
    val x2 = high.hundredthVolt
    val y2 = high.reading

    Seq(
      /*
      *2064 C * M* X1* Y1* X2* Y2* C= Channel 1 to 8 M=Meter Type 0 to 6 X1, Y1, X2, Y2 represent two calibration points.
      * There must be 6 parameters entered to define a meterEditor face, each value ending with *.
       */
      s"1*2064$c*$m*$x1*$y1*$x2*$y2*"
    )
  }

  override def toJsValue: JsValue = Json.toJson(this)

  override def toRow: Row = Row(
    ButtonCell.edit(fieldKey),
    fieldKey.key.keyWithName,
    meterFaceName,
    low.hundredthVolt,
    low.reading,
    high.hundredthVolt,
    high.reading,
  )

object MeterNode extends ComplexFieldDefinition[MeterNode]:
  val keyKind = KeyKind.Meter

  def unapply(u: MeterNode): Option[(Key, MeterFaceName, VoltToReading, VoltToReading)] = Some((u.key, u.meterFaceName, u.low, u.high))

  override val form: Form[MeterNode] = Form(
    mapping(
      "key" -> of[Key],
      "faceName" -> MeterFaceName.formField,
      "low" -> VoltToReading.form,
      "high" -> VoltToReading.form,
    )(MeterNode.apply)(MeterNode.unapply))

  def extract(memory: Memory): Seq[FieldEntry] = {
    val mai = new AtomicInteger()
    val nMeters = KeyKind.Meter.maxN
    val faceInts = memory.sub8(186, nMeters)
    val faceNames: Seq[MeterFaceName] = faceInts.map(i => MeterFaceName.find(i))
    val lowX: Seq[Int] = memory.iterator16At(202).take(nMeters).toSeq
    val lowY: Seq[Int] = memory.iterator16At(218).take(nMeters).toSeq
    val highX: Seq[Int] = memory.iterator16At(234).take(nMeters).toSeq
    val highY: Seq[Int] = memory.iterator16At(250).take(nMeters).toSeq

    val meters: Seq[FieldEntry] = for {
      i <- 0 until nMeters
    } yield {
      val meterKey: Key = Key(KeyKind.Meter, mai.incrementAndGet())
      val low = VoltToReading(lowX(i), lowY(i))
      val high = VoltToReading(highX(i), highY(i))
      val meter: MeterNode = MeterNode(meterKey, faceNames(i), low, high)
      FieldEntry(this, meter.fieldKey, meter)
    }
    meters
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[MeterNode]

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(186, this, "meterFace"),
    FieldOffset(202, this, "meterLowVolt"),
    FieldOffset(218, this, "meterLowReading"),
    FieldOffset(282, this, "alarm Set Point"),
    FieldOffset(2064, this, "meterEditor"),
  )

  implicit val fmtVoltToReading: Format[VoltToReading] = Json.format[VoltToReading]
  implicit val fmtMeter: Format[MeterNode] = Json.format[MeterNode]

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val header = Seq(
      Seq(Cell(s"Meters  (${fieldEntries.length})").withColSpan(7)),
      Seq(
        Cell("").withRowSpan(2),
        Cell("Meter").withRowSpan(2),
        Cell("Face").withRowSpan(2),
        Cell("Low").withColSpan(2),
        Cell("High").withColSpan(2)
      ),
      Seq(
        Cell("Volt"),
        Cell("Reading"),
        Cell("Volt"),
        Cell("Reading"),
      ),
    )
    fieldIndex(keyKind, Table(header, fieldEntries.map(_.value.toRow)))

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    meterEditor(form.fill(fieldEntry.value), fieldEntry.fieldKey)

  override def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate] =
    bind(form.bindFromRequest(data).get)

/**
 *
 * @param hundredthVolt  input voltage
 * @param reading        what "shows" on the4 meterEditor face.
 */
case class VoltToReading(hundredthVolt: Int, reading: Int)

object VoltToReading:
  def unapply(u: VoltToReading): Option[(Int, Int)] = Some((u.hundredthVolt, u.reading))

  val form: Mapping[VoltToReading] =
    mapping(
      "hundredthVolt" -> number,
      "reading" -> number,
    )(VoltToReading.apply)(VoltToReading.unapply)


