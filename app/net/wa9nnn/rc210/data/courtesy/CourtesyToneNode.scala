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

package net.wa9nnn.rc210.data.courtesy

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table}
import controllers.routes
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue, FieldEntry, FieldEntryBase, FieldOffset, FieldValue}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.ButtonCell
import play.api.data.*
import play.api.data.Forms.*
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json, OFormat}
import play.api.mvc.{RequestHeader, Result, Results}
import play.twirl.api.Html
import views.html.{courtesyToneCell, courtesyToneEdit, courtesyTones, fieldIndex, named}

import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @param key      id
 * @param segments the four segment that mke up a courtesy tone.
 */
case class CourtesyToneNode(override val key: Key, segments: Seq[Segment]) extends ComplexFieldValue() {
  assert(segments.length == 4, s"Must have four segemnts but only have: $segments")

  override def displayCell: Cell =
    given CourtesyToneNode = this

    val html = courtesyToneCell(this)
    Cell.rawHtml(html.body)

  implicit val k: Key = key
  implicit val s: Seq[Segment] = segments

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val segN = new AtomicInteger(1)
    segments.map { segment =>
      segment.toCommand(key.rc210Value, segN.getAndIncrement())
    }
  }

  override def toJsValue: JsValue = Json.toJson(this)

  override def toRow: Row = Row(
    ButtonCell.edit(fieldKey),
    named(key),

  )
}

/**
 * [[CourtesyToneNode]] data are spread out in the [[Memory]] image.
 * This gaters all the data and produces all the [[CourtesyToneNode]]s.
 */
object CourtesyToneNode extends ComplexExtractor[CourtesyToneNode] with LazyLogging:
  override val keyKind: KeyKind = KeyKind.CourtesyTone
  private val nCourtesyTones = keyKind.maxN

  def unapply(u: CourtesyToneNode): Option[(Key, Seq[Segment])] = Some((u.key, u.segments))

  val form: Form[CourtesyToneNode] = Form(
    mapping(
      "key" -> of[Key],
      "segment" -> seq(Segment.form),
    )(CourtesyToneNode.apply)(CourtesyToneNode.unapply)
  )
  implicit val fmtCourtesyTone: Format[CourtesyToneNode] = Json.format[CourtesyToneNode]

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(856, this),
  )

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] =
    logger.debug("Extract CourtesyTone")
    val iterator = memory.iterator16At(856)

    val array = Array.ofDim[Int](10, 16)
    for
    {
      part <- 0 until 16
      ct <- 0 until nCourtesyTones
    }
    {
      array(ct)(part) = iterator.next()
    }

    val courtesyTones: Seq[CourtesyToneNode] =
      for ct <- 0 until 10 yield
      {
        val key: Key = Key(KeyKind.CourtesyTone, ct + 1)
        val segments = Seq(
          Segment(array(ct)(8), array(ct)(12), array(ct)(0), array(ct)(1)),
          Segment(array(ct)(9), array(ct)(13), array(ct)(2), array(ct)(3)),
          Segment(array(ct)(10), array(ct)(14), array(ct)(4), array(ct)(5)),
          Segment(array(ct)(11), array(ct)(15), array(ct)(6), array(ct)(7)),
        )
        CourtesyToneNode(key, segments)
      }
    courtesyTones.map { (courtesyToneNode: CourtesyToneNode) =>
      FieldEntry(this, courtesyToneNode)
    }

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[CourtesyToneNode]

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html = {
    val seq1: Seq[CourtesyToneNode] = fieldEntries.map(_.value[CourtesyToneNode])
    val html = courtesyTones(seq1)
    html
  }

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    given Form[CourtesyToneNode] = form.fill(fieldEntry.value)

    courtesyToneEdit(fieldEntry.fieldKey)

  override def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate] =
    val courtesyTone = form.bindFromRequest(data).get
    Seq(
      UpdateCandidate(courtesyTone.fieldKey, courtesyTone)
    )


