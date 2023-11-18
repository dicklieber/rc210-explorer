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

import com.wa9nnn.util.tableui.{Cell, CellProvider}
import net.wa9nnn.rc210.data.field.FieldKey
import net.wa9nnn.rc210.data.named.NamedKeySource
import net.wa9nnn.rc210.util.{SelectItem, SelectItemNumber}
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.mvc.PathBindable
import play.twirl.api.Html
import net.wa9nnn.rc210.KeyKind
//import scala.reflect.ClassTag

/**
 *
 * @param keyKind of the Key
 * @param number  0 is a magic number used for things like [[KeyKind.commonKey]]
 */
case class Key(keyKind: KeyKind, number: Int = 0) extends  CellProvider with NamedKeySource with SelectItemNumber {
  def check(target: KeyKind): Unit = if (target != keyKind) throw new WrongKeyType(this, target)

      assert(number <= keyKind.maxN, s"Max number for ${keyKind.name} is ${keyKind.maxN}")

  override def toString: String = s"$keyKind$number"

  override def compare(that: Key): Int =
    var ret = keyKind compareTo that.keyKind
    if (ret == 0)
      ret = number compareTo that.number
    ret

  def fieldKey(fieldName: String): FieldKey = FieldKey(fieldName, this)

  def namedCell(param: String = fieldKey("name").param): Cell =
    val html: Html = views.html.fieldNamedKey(this, nameForKey(this), param)
    Cell.rawHtml(html.toString())

  def keyWithName: String = s"$number ${nameForKey(this)}"

  override def toCell: Cell = {
    val name = nameForKey(this)
    val c = if (name.isEmpty)
      Cell(number)
    else
      Cell(s"$number: $name")
    c.withCssClass(keyKind.toString)
  }

  /**
   * Replaces 'n' in the template with the number (usually a port number).
   *
   * @param template in
   * @return with 'n' replaced by the port number.
   */
  def replaceN(template: String): String = {
    template.replaceAll("n", number.toString)
  }

  override def nameForKey(key: Key): String = ???
}

object Key:
  private val kparser = """(\D+)(\d*)""".r

  def apply(sKey: String): Key =
    sKey match
      case kparser(sKind, sNumber) =>
        val keyKind = KeyKind.valueOf(sKind)
        new Key(keyKind, sNumber.toInt)

  def setNamedSource(namedSource: NamedKeySource): Unit = {
    if (_namedSource.isDefined) throw new IllegalStateException("NamedSource already set.")
    _namedSource = Option(namedSource)
  }

  private var _namedSource: Option[NamedKeySource] = None

  def nameForKey(key: Key): String =
    _namedSource.map(_.nameForKey(key)).getOrElse("")

  /**
   * Codec to allow non-string types i routes.conf definitions.
   */

  implicit def keyKindPathBinder: PathBindable[KeyKind] = new PathBindable[KeyKind] {
    override def bind(key: String, value: String): Either[String, KeyKind] = {
      Right(KeyKind.valueOf(value))
    }

    override def unbind(key: String, macroKey: KeyKind): String = {
      macroKey.toString
    }
  }

case class WrongKeyType(key: Key, expected: KeyKind) extends IllegalArgumentException(s"Expecting Key of type ${expected.name}, but got $key}")

