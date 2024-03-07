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

package net.wa9nnn.rc210

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.data.EditHandler
import play.api.libs.json.*

import scala.util.Try

/**
 * Identifies a rc2input value
 *
 * @param fieldName      name of rc2input
 * @param key            qualifier for the rc2input.
 */
case class FieldKey(key: Key, fieldName: String = "") extends Ordered[FieldKey] {
  fieldName match
    case _ if key.keyKind.needsFieldName =>
      assert(fieldName.nonEmpty, "Requires a fieldname!  (Port or Common)")
    case _ if fieldName == NamedKey.fieldName =>
    case _ =>
      assert(fieldName.isEmpty, s"Can't have a fieldName for a ${key.keyKind.entryName}")

  override def compare(that: FieldKey): Int =
    var ret: Int = key.keyKind.toString compareTo that.key.keyKind.toString
    if (ret == 0)
      ret = key.rc210Value.compareTo(that.key.rc210Value)
    if (ret == 0)
      fieldName.compare(that.fieldName)
    ret

  def editButtonCell: Cell = key.keyKind.handler.editButtonCell(this)

  /**
   * | Example | Comment |
   * | :---:  | --- |
   * | '''Port1:DTMF Cover Tone''' |
   * | Clock | Complex [[net.wa9nnn.rc210.data.field.FieldValue]]
   * | Site Pref  ix | Associated with a [[KeyKind.Common]]
   */
  val display: String =
    val r = if (key.keyKind == KeyKind.Common)
      fieldName
    else if (fieldName.nonEmpty)
      key.entryName + ": " + fieldName
    else {
      val bool = key.keyKind.maxN == 1
      if (bool)
        key.keyKind.entryName
      else
        key.toString
    }
    r

  /**
   * Used for things and JSON and HTML field names.
   */
  val id: String =
    s"${key.toString}$$$fieldName"

  val editHandler: EditHandler =
    key.keyKind.handler
}

object FieldKey {
  /**
   * When used in a form.
   */
  val fieldKeyName: String = "fieldKey"

  implicit val fmtFieldKey: Format[FieldKey] = new Format[FieldKey] {
    override def writes(o: FieldKey) = JsString(o.id)

    override def reads(json: JsValue): JsResult[FieldKey] = {
      JsResult.fromTry(Try {
        val id = json.as[String]
        FieldKey.fromId(id)
      })
    }
  }

  /**
   *
   * @param id from [[FieldKey.id]]
   * @return
   */
  def fromId(id: String): FieldKey =
    id match
      case r(sKey, fieldName) =>
        val key = Key(sKey)
        FieldKey(key, fieldName)
      case s: String =>
        throw new scala.IllegalArgumentException(s"Can't parse: $id")

  private val r = """\$([\w ]+)\$(.*)""".r
}