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
import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.data.field.{FieldDefComplex, FieldEntry, FieldOffset, FieldValue, FieldValueComplex, TemplateSource}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.{ButtonCell, FormData}
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
case class CourtesyToneNode(segments: Seq[Segment]) extends FieldValueComplex[CourtesyToneNode]():
  assert(segments.length == 4, s"Must have four segemnts but only have: $segments")

  override def displayCell: Cell =
    given CourtesyToneNode = this

    val html = courtesyToneCell(this)
    Cell.rawHtml(html.body)

//  implicit val k: Key = key
//  implicit val s: Seq[Segment] = segments

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntry): Seq[String] =
    val segN = new AtomicInteger(1)
    val key = fieldEntry.key
    segments.map { segment =>
      segment.toCommand(key.rc210Number.get, segN.getAndIncrement())
    }

  override def toRow(key: Key): Row = Row(
    ButtonCell.edit(key),
    named(key),
  )

/**
 * [[CourtesyToneNode]] data are spread out in the [[Memory]] image.
 * This gaters all the data and produces all the [[CourtesyToneNode]]s.
 */
object CourtesyToneNode extends FieldDefComplex[CourtesyToneNode] with LazyLogging:
  override val keyMetadata: KeyMetadata = KeyMetadata.CourtesyTone
  private val nCourtesyTones = keyMetadata.maxN

  def unapply(u: CourtesyToneNode): Option[(Seq[Segment])] = Some((u.segments))

  val form: Form[CourtesyToneNode] = Form(
    mapping(
      "segment" -> seq(Segment.form),
    )(CourtesyToneNode.apply)(CourtesyToneNode.unapply)
  )
  implicit val fmt: Format[CourtesyToneNode] = Json.format[CourtesyToneNode]

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

    for (ct <- 0 until 10) yield
    {
      val key: Key = Key(KeyMetadata.CourtesyTone, ct + 1)
      val segments = Seq(
        Segment(array(ct)(8), array(ct)(12), array(ct)(0), array(ct)(1)),
        Segment(array(ct)(9), array(ct)(13), array(ct)(2), array(ct)(3)),
        Segment(array(ct)(10), array(ct)(14), array(ct)(4), array(ct)(5)),
        Segment(array(ct)(11), array(ct)(15), array(ct)(6), array(ct)(7)),
      )
      val courtesyToneNode = CourtesyToneNode(segments)
      FieldEntry(this, key, courtesyToneNode)
    }

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html = {
    val html = courtesyTones(fieldEntries.map(_.value[CourtesyToneNode]))
    html
  }

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    given Form[CourtesyToneNode] = form.fill(fieldEntry.value)

    courtesyToneEdit(fieldEntry.key)

  override def bind( formData: FormData): Seq[UpdateCandidate] =
    val key = formData.key
    val courtesyTone = form.bindFromRequest(formData.map).get
    Seq(
      UpdateCandidate(key, courtesyTone)
    )


