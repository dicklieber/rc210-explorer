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
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, KvTable, Row, Table, TableSection}
import controllers.routes
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.*
import net.wa9nnn.rc210.{ Key, KeyMetadata}
import play.api.data.Form
import play.api.data.Forms.{mapping, number, of}
import play.api.i18n.MessagesProvider
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import views.html.{fieldIndex, timerEditor}

import scala.concurrent.duration.{Duration, FiniteDuration}

case class TimerNode(seconds: Int, macroKey: Key) extends FieldValueComplex(macroKey) :
  val duration: FiniteDuration = Duration(seconds, "seconds")

  override def toRow(key: Key): Row = Row(
    ButtonCell.edit(key),
    seconds,
    macroKey.keyWithName
  )

  private val rows: Seq[Row] = Seq(
    "Duration" -> duration,
    "Macro" -> macroKey.keyWithName,
  ).map(Row(_))

  override def tableSection(key: Key): TableSection = {
    TableSectionButtons(key, rows: _*)
  }

  override def displayCell: Cell =
    KvTable.inACell(rows:_*)

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntry): Seq[String] = {
    val key = fieldEntry.key
    val timeNumber: Int = key.rc210Number.get
    val secs: Int = seconds
    val macroNumber = macroKey.rc210Number
    Seq(
      s"1*1017$timeNumber$secs",
      s"1*2092$timeNumber$macroNumber"
    )
  }


object TimerNode extends FieldDefComplex[TimerNode] with LazyLogging:

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
      index <- 0 until KeyMetadata.Timer.maxN
    } yield {
      val key: Key = Key(KeyMetadata.Timer, index + 1)
      FieldEntry(this, key, TimerNode(seconds.next(), Key(KeyMetadata.Macro, macroInts.next() + 1)))
    })
    r
  }

  def unapply(u: TimerNode): Option[( Int, Key)] = Some(( u.seconds, u.macroKey))

  val form: Form[TimerNode] = Form[TimerNode](
    mapping(
      "seconds" -> number,
      "macroKey" -> of[Key],
    )((seconds: Int, macroKey: Key) => TimerNode.apply(seconds, macroKey))(TimerNode.unapply)
  )

  implicit val fmt: OFormat[TimerNode] = Json.format[TimerNode]
  override val keyMetadata: KeyMetadata = KeyMetadata.Timer

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    fieldIndex(keyMetadata, Table(Header(s"Timers  (${fieldEntries.length})",
      "",
      "Timer",
      "Seconds",
      "Macro"
    ),
      fieldEntries.map(fe => {
        val value:TimerNode = fe.value
        value.toRow(fe.key)
      })
    ))

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    timerEditor(form.fill(fieldEntry.value), fieldEntry.key)

  override def bind(formData: FormData): Seq[UpdateCandidate] =
    val timerNode: TimerNode = form.bindFromRequest(formData.map).get
    Seq(UpdateCandidate(formData.key, timerNode))


