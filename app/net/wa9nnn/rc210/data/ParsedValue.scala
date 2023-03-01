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

package net.wa9nnn.rc210.data

import com.wa9nnn.util.tableui.{Cell, CellProvider}
import net.wa9nnn.rc210.key.KeyFormats.parseString
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.key.{Key, KeyFormats}
import net.wa9nnn.rc210.util.CamelToWords
import play.api.libs.json.{Format, JsResult, JsString, JsValue}
import play.api.mvc.PathBindable

import scala.util.Try

/**
 * Identifies a field value
 *
 * @param fieldName name of field. Shown in UIs
 * @param key       qualifier for the field.
 */
case class FieldKey(fieldName: String, key: Key) extends Ordered[FieldKey] with CellProvider{
  /**
   * can identify this in a HTTP param or as a JSON name.
   */
  val param: String = s"$fieldName|$key"
  val prettyName:String = CamelToWords(fieldName)

  override def compare(that: FieldKey): Int = {
    var ret = key.toString compareTo(that.key.toString)
    if (ret == 0)
      ret = fieldName.compareTo(that.fieldName)
    ret
  }

  override def toCell: Cell = {
    Cell(prettyName)
  }
}

object FieldKey {

  implicit val fmtFieldKey: Format[FieldKey] = new Format[FieldKey] {
    override def writes(o: FieldKey) = JsString(o.param)



/*
    override def reads(json: JsValue): JsResult[FieldKey] = {
      JsResult.fromTry(Try {
        KeyFormats.parseString(json.as[String])
      })
    }
*/

    override def reads(json: JsValue): JsResult[FieldKey] = {
      JsResult.fromTry(Try {
        fromParam(json.as[String])
      })
    }
  }

  implicit def fieldKeyPathBinder(implicit intBinder: PathBindable[FieldKey]): PathBindable[FieldKey] = new PathBindable[FieldKey] {
    override def bind(key:String, fromPath: String): Either[String, FieldKey] = {
      try {
        Right(fromParam(fromPath))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }
    }

    override def unbind(key: String, fieldKey: FieldKey): String =
      fieldKey.param
  }

  def fromParam(param: String): FieldKey = {
    val r(fieldName, sKey) = param
    FieldKey(fieldName, KeyFormats.parseString(sKey))
  }

  private val r = """([a-zA-Z]+)\|(.*)""".r
}


case class ParsedValue(fieldKey: FieldKey, value: String)
