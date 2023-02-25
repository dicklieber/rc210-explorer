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

package net.wa9nnn.rc210.command

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.command.ItemValue.Values
import net.wa9nnn.rc210.key.{Key, MiscKey, PortKey}

import scala.util.{Failure, Success, Try}

/**
 *
 * @param values                 as parsed or inputted.
 * @param key                    used when there is more than one [[ItemValue]].
 * @param maybeMessage           if there was an error parsing or from processing the form value. This can be localized.
 */
case class ItemValue(commandId: Command, values: Values, key:Key = MiscKey(), maybeMessage: Option[L10NMessage] = None) extends Ordered[ItemValue] with RowSource{

  def head: String = values.headOption.getOrElse("?")

  def withKey(key: Key): ItemValue = copy(key = key)
  def withPort(number:Int): ItemValue = copy(key = PortKey(number + 1))

  def withError(l10NMessage: L10NMessage): ItemValue = copy(maybeMessage = Option(l10NMessage))

  override def toString: String = {
//    val sError = maybeMessage.map(e => s" error: $maybeMessage").getOrElse("")
    s"commandId: $commandId  key:$key value: ${values.mkString(", ")}"
  }

  override def compare(that: ItemValue): Int = commandId compareTo that.commandId

  override def toRow: Row = Row(commandId.toString,values.mkString(","), key, maybeMessage)
}

object ItemValue {
  type Values = Seq[String]
  def header(count:Int): Header = Header(s"Item Values ($count)", "Command", "Values", "Key", "Message")

  def apply(commandId: Command, value: String): ItemValue = {
    new ItemValue(commandId, Seq(value))
  }

  def apply(commandId: Command, triedValues: Try[Values]): ItemValue = {
    triedValues match {
      case Failure(exception) =>
        exception match {
          case L10NParseException(l10NError) =>
            new ItemValue(commandId, Seq("?"), maybeMessage = Option(l10NError))
          case e =>
            new ItemValue(commandId, Seq("?"), maybeMessage = Option(L10NMessage(e.getMessage)))
        }
      case Success(value) =>
        new ItemValue(commandId, value)
    }
  }

 }



