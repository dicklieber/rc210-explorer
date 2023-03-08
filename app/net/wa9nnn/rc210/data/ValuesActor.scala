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
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.ValuesActor.{AllDataEnteries, SetValue, SetValues}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.mapped.MappedValues
import play.api.Configuration

import javax.inject.Inject

/**/
object ValuesActor extends LazyLogging {
  trait ValueMessage

  case class SetValue(fieldKey: FieldKey, value: String)

  case class SetValues(values: Seq[SetValue])

  case object AllDataEnteries

  case class InitialData(data:Seq[FieldEntry]) extends ValueMessage
}


class ValuesActor @Inject()() extends Actor with LazyLogging {
  val values = new MappedValues()

  def receive: Receive = {
    case fv: SetValue =>
      values.update(fv.fieldKey, fv.value)

    case setValues: SetValues =>
      values.update(setValues)
    case AllDataEnteries =>
      sender() ! values.all.toSeq

    case _ =>
      logger.warn("Received something i don't know")
  }
}

//  def receive = {
//    case GetConfig =>
//      sender() ! config
//  }
//}