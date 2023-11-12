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

package net.wa9nnn.rc210.key

import com.wa9nnn.util.tableui.{Cell, CellProvider}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.named.NamedKeySource
import net.wa9nnn.rc210.key.KeyKind.*
import play.twirl.api.Html
sealed abstract class Key(val number: Int, val keyKind: KeyKind) extends Ordered[Key] with CellProvider with NamedKeySource {

  override def toString: String = s"$keyKind$number"

  override def compare(that: Key): Int =
    var ret = keyKind compareTo that.keyKind
    if (ret == 0)
      ret = number compareTo that.number
    ret

  def fieldKey(fieldName: String): FieldKey = FieldKey(fieldName, this)

  def namedCell(param: String = fieldKey("name").param): Cell = {
    val html: Html = views.html.fieldNamedKey(this, nameForKey(this), param)
    Cell.rawHtml(html.toString())
  }

  def keyWithName: String = {
    s"$number ${nameForKey(this)}"
  }


  override def toCell: Cell = {
    val name = nameForKey(this)
    val c = if (name.isEmpty)
      Cell(number)
    else
      Cell(s"$number: $name")
    c.withCssClass(keyKind.toString)
  }

  /**
   * Replace's 'n' in the template with the number (usually a port number).
   *
   * @param template in
   * @return with 'n' replaced by the port number.
   */
  def replaceN(template: String): String = {
    template.replaceAll("n", number.toString)
  }


  def setNamedSource(namedsource: NamedKeySource): Unit = {
    if (_namedSource.isDefined) throw new IllegalStateException("NamedSource already set.")
    _namedSource = Option(namedsource)
  }

  var _namedSource: Option[NamedKeySource] = None

  def nameForKey(key: Key): String =
    _namedSource.map(_.nameForKey(key)).getOrElse("")

}

abstract class SingleKey(keyKind: KeyKind) extends Key(0, keyKind):
  override def toString: String = keyKind.toString


case class MacroKey(number: Int) extends Key(number, KeyKind.macroKey)

case class PortKey(number: Int) extends Key(number, KeyKind.portKey)

case class LogicAlarmKey(number: Int) extends Key(number, KeyKind.logicAlarmKey)

case class MeterKey(number: Int) extends Key(number, KeyKind.meterKey)

case class MeterAlarmKey(number: Int) extends Key(number, KeyKind.meterAlarmKey)

case class MessageKey(number: Int) extends Key(number, KeyKind.messageKey)

case class FunctionKey(number: Int) extends Key(number, KeyKind.functionKey)

case class ScheduleKey(number: Int) extends Key(number, KeyKind.scheduleKey)

case class DtmfMacroKey(number: Int) extends Key(number, KeyKind.dtmfMacroKey)

case class CourtesyToneKey(number: Int) extends Key(number, KeyKind.courtesyToneKey)

case class TimerKey(number: Int) extends Key(number, KeyKind.timerKey)

case object CommonKey extends SingleKey(KeyKind.commonKey)

case object ClockKey extends SingleKey(KeyKind.clockKey)

case object RemoteBaseKey extends SingleKey(KeyKind.remoteBaseKey)

