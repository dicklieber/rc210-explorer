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

package net.wa9nnn.rc210.data.timers

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.TableSection
import net.wa9nnn.rc210.data.TriggerNode
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.{mapping, number, of}
import play.api.libs.json.{JsValue, Json, OFormat}

import scala.concurrent.duration.{Duration, FiniteDuration}

case class TimerNode(key: Key, seconds: Int, macroKey: Key) extends ComplexFieldValue() with TriggerNode(macroKey) {
  val duration: FiniteDuration = Duration(seconds, "seconds")
  //  override def displayHtml: String = //s"$seconds => ${macroKey.keyWithName}"

  override def tableSection(fieldKey: FieldKey): TableSection =
    TableSection(s"Timer ${key.keyWithName}",
      "Duration" -> duration
    )

  override def displayHtml: String =
    <table class="tagValuetable">
      <tr>
        <td>Timer</td>
        <td>
          {key.keyWithName}
        </td>
      </tr>
      <tr>
        <td>time</td>
        <td>
          {duration}
        </td>
      </tr>
    </table>.toString

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val timeNumber: Int = key.rc210Value
    val secs: Int = seconds
    val macroNumber = macroKey.rc210Value
    Seq(
      s"1*1017$timeNumber$secs",
      s"1*2092$timeNumber$macroNumber"
    )
  }

  override def toJsValue: JsValue = Json.toJson(this)

  override def canRunMacro(candidate: Key): Boolean =
    macroKey eq candidate

}

//noinspection ZeroIndexToHead
object TimerNode extends ComplexExtractor[TimerNode] with LazyLogging {
  //  private val nTimers = keyKind.maxN
  //  Memory Layout
  //  seconds for each timer 6 2-byte ints
  //  macroToRun for each timer 6 1-byte ints

  override def positions: Seq[FieldOffset] = {
    Seq(
      FieldOffset(1553, this),
      FieldOffset(1565, this)
    )
  }

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val seconds: Iterator[Int] = memory.iterator16At(1553)
    val macroInts: Iterator[Int] = memory.iterator8At(1565)

    val r: Seq[FieldEntry] = (for {
      index <- 0 until KeyKind.Timer.maxN
    } yield {
      val key: Key = Key(KeyKind.Timer, index + 1)
      FieldEntry(this, fieldKey(key), TimerNode(key, seconds.next(), Key(KeyKind.RcMacro, macroInts.next() + 1)))
    })
    r
  }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[TimerNode]

  override val name: String = "Timer"
  override val fieldName: String = name

  def unapply(u: TimerNode): Option[(Key, Int, Key)] = Some((u.key, u.seconds, u.macroKey))

  val form: Form[TimerNode] = Form[TimerNode](
    mapping(
      "key" -> of[Key],
      "seconds" -> number,
      "macroKey" -> of[Key],
    )(TimerNode.apply)(TimerNode.unapply)
  )

  implicit val fmtTimer: OFormat[TimerNode] = Json.format[TimerNode]
  override val keyKind: KeyKind = KeyKind.Timer
}
