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

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.DataProvider
import net.wa9nnn.rc210.data.ValuesStore.{AllDataEnteries, InitialData, SetValues, Value, Values}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.mapped.MappedValues
import net.wa9nnn.rc210.key.KeyKindEnum.KeyKind

import javax.inject.Inject

/**/
object ValuesStore extends LazyLogging {
  trait ValueStoreMessage

  case class SetValue(fieldKey: FieldKey, value: String)

  case class SetValues(values: Seq[SetValue]) extends ValueStoreMessage

  case object AllDataEnteries
  case class Values(keyKind: KeyKind) extends ValueStoreMessage
  case class Value(keyKind: FieldKey) extends ValueStoreMessage

  case class InitialData(data:Seq[FieldEntry]) extends ValueStoreMessage
}


class ValuesStore @Inject()(dataProvider: DataProvider) extends Actor with LazyLogging {
  var values: MappedValues = new MappedValues(dataProvider.ife)

  def receive: Receive = {
    case Value(fieldKey) =>
      sender() ! values.entity(fieldKey)
    case Values(keyKind: KeyKind) =>
      val result: Seq[FieldEntry] = values.all.filter(_.fieldKey.key.kind == keyKind)
      sender() ! result
    case setValues: SetValues =>
      values.update(setValues)
    case AllDataEnteries =>
      sender() ! values.all

    case InitialData(fieldEntries: Seq[FieldEntry]) =>
      values =  new MappedValues(fieldEntries)

    case x =>
      logger.warn(s"Received something i don't know: $x")
  }
}

