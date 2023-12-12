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

package net.wa9nnn.rc210.data.remotebase

import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.util.Chunk
import net.wa9nnn.rc210.{Key, KeyKind}
import play.api.data.Forms.*
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.*

case class RemoteBase(radio: Radio, yaesu: Yaesu, prefix: String, memories: Seq[RBMemory] = Seq.empty) extends ComplexFieldValue("RemoteBase") {
  override val key: Key = Key(KeyKind.remoteBaseKey)

  //  override def display: String = fieldName

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = Seq(
    s"1*2083${radio.rc210Value}",
    s"1*2084${yaesu.rc210Value}",
    s"1*2060$prefix"
  )

  override def toJsValue: JsValue = Json.toJson(this)

  override def displayHtml: String = toString
}

object RemoteBase extends ComplexExtractor {
  def unapply(u: RemoteBase): Option[(Radio, Yaesu, String, Seq[RBMemory])] = Some((u.radio, u.yaesu, u.prefix, u.memories))

  val form: Form[RemoteBase] = Form(
    mapping(
      "radio" -> Radio.formField,
      "yaesu" -> Yaesu.formField,
      "prefix" -> text,
      "memories" -> seq(RBMemory.form),
    )(RemoteBase.apply)(RemoteBase.unapply))

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    //    SimpleField(1176, "Radio Type", commonKey, "n*2083 v", RadioType)
    //    SimpleField(1177, "Yaesu Type", commonKey, "n*2084 v", YaesuType
    //    SimpleField(3525, "Remote Base Prefix", commonKey, "n*2060v", FieldDtmf) max 5,
    ////FreqString - 3562-3641	remote base stuff
    ////InactivityMacro - 1545-1550

    val radio: Radio = Radio.find(memory(1176))
    val yaesu: Yaesu = Yaesu.find(memory(1177))
    val prefix = memory.stringAt(3525)

    val freqs: Seq[String] = memory.chunks(3562, 8, 10).map((chunk: Chunk) => chunk.toString)
    //    val offsets: Seq[Offset] = memory.sub8(3562, 10).map { Offset(_)}
    val ctcsss: Seq[Int] = memory.sub8(3642, 10)
    val ctcsssModes: Seq[CtcssMode] = memory.sub8(3652, 10).map(CtcssMode.find)
    val modes: Seq[Mode] = memory.sub8(1535, 10).map { (n: Int) =>
      Mode.find(n)
    }
    val memories: IndexedSeq[RBMemory] = for {
      i <- 0 until 10
    } yield {
      val freqOffset: String = freqs(i)
      val offset = Offset.find(freqOffset.last.asDigit)
      val freq = freqOffset.dropRight(1)
      RBMemory(
        frequency = freq,
        offset = offset,
        mode = modes(i),
        ctcssMode = ctcsssModes(1),
        ctssCode = ctcsss(i),
      )
    }

    val remoteBase = RemoteBase(radio, yaesu, prefix, memories)
    Seq(FieldEntry(this, remoteBase.fieldKey, remoteBase))
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[RemoteBase]


  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(1176, this, "Radio Type"),
    FieldOffset(1177, this, "Yaesu"),
    FieldOffset(3525, this, "Remote Base Prefix"),
    FieldOffset(3562, this, "Frequencies"),
    FieldOffset(3562, this, "Offset"),
  )

  /**
   * for various things e.g. parser name.
   */
  override val name: String = "RemoteBase"
  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.remoteBaseKey
  val key:Key = Key(kind)
  val fieldKey: FieldKey = super.fieldKey(key)

  implicit val fmtRBMemory: Format[RBMemory] = Json.format[RBMemory]
  implicit val fmtRemoteBase: Format[RemoteBase] = Json.format[RemoteBase]

}

/**
 *
 * @param frequency no decimal point. e.g. 146.52 is "146520"
 * @param offset
 * @param mode
 * @param ctcssMode
 * @param ctssCode  See appendix B
 */
case class RBMemory(frequency: String, offset: Offset, mode: Mode, ctcssMode: CtcssMode, ctssCode: Int)

object RBMemory:
  def unapply(u: RBMemory): Option[(String, Offset, Mode, CtcssMode, Int)] = Some((u.frequency, u.offset, u.mode, u.ctcssMode, u.ctssCode))

  val form: Mapping[RBMemory] =
    mapping(
      "frequency" -> text,
      "offset" -> Offset.formField,
      "mode" -> Mode.formField,
      "ctcssMode" -> CtcssMode.formField,
      "ctcssCode" -> number,
    )(RBMemory.apply)(RBMemory.unapply)
