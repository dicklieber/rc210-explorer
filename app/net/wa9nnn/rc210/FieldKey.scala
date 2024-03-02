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
import net.wa9nnn.rc210.FieldKey.fieldKeyName
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.libs.json.*

import scala.util.Try

/**
 * Identifies a rc2input value
 *
 * @param fieldName name of rc2input. Shown in UIs
 * @param key       qualifier for the rc2input.
 */
case class FieldKey(key: Key, fieldName: String) extends Ordered[FieldKey] {

  override def compare(that: FieldKey): Int = {
    var ret = key.keyKind.toString compareTo that.key.keyKind.toString
    if (ret == 0)
      ret = key.rc210Value.compareTo(that.key.rc210Value)
    if (ret == 0)
      ret = fieldName.compareTo(that.fieldName)
    ret
  }

  def editButtonCell: Cell = key.keyKind.handler.editButtonCell(this)

  override def toString: String =
    if(key.rc210Value == 0)
      fieldName
    else if(key.keyKind.entryName == fieldName)
      key.toString
    else
      s"${key.toString}:$fieldName"

  val editHandler: EditHandler =
    key.keyKind.handler
}

object FieldKey {
  /**
   * When used in a form.
   */
  val fieldKeyName: String = "fieldKey"

  /**
   * For use field name is the [[KeyKind]] name.
   *
   * @param key whose [[KeyKind]] name is the field name.
   */
  def apply(key: Key): FieldKey =
    new FieldKey(key, key.keyKind.entryName)

  implicit val fmtFieldKey: Format[FieldKey] = new Format[FieldKey] {
    override def writes(o: FieldKey) = JsString(o.toString)

    override def reads(json: JsValue): JsResult[FieldKey] = {
      JsResult.fromTry(Try {
        val sFieldKey = json.as[String]
        FieldKey(sFieldKey)
      })
    }
  }

  def opt(maybeSFieldKey: Option[String]): Option[FieldKey] =
    maybeSFieldKey.map(FieldKey(_))

  def apply(param: String): FieldKey = {
    param match
      case justKey(sKey) =>
        val key = Key(sKey)
        FieldKey(key)
      case r(sKey, fieldName) =>
        val key = Key(sKey)
        FieldKey(key, fieldName)
      case s: String =>
        val keyKind = KeyKind.withName(s)
        val key = Key(keyKind)
        val fk=FieldKey(key)
        fk

  }

  private val r = """(.+\d):(.*)""".r
  private val justKey = """([ \w]+\d+)""".r
}