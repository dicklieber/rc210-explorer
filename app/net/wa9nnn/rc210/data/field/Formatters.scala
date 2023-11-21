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

package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.Dtmf
import net.wa9nnn.rc210.data.clock.Occurrence
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.data.remotebase.{Mode, Offset}
import net.wa9nnn.rc210.key.*
import net.wa9nnn.rc210.util.select.Rc210Item
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.{Format, Json, OFormat}

import scala.collection.immutable.Nil

/**
 * URL formatters.
 * Converts HTML form values to and from application objects.
 */
object Formatters {

  import play.api.data.format.Formats._
  import play.api.data.format.Formatter





//  implicit object DtmfFormatter extends Formatter[Dtmf] {
//    override val format: Option[(String, Nil.type)] = Some(("format.dtmf", Nil))
//
//    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Dtmf] = parsing(Dtmf(_), "error.url", Nil)(key, data)
//
//    override def unbind(key: String, value: Dtmf): Map[String, String] = Map(key -> value.toString)
//  }





//  implicit object TimerKeyFormatter extends Formatter[TimerKey] {
//    override val format: Option[(String, Nil.type)] = Some(("format.TimerKey", Nil))
//
//    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], TimerKey] =
//      parsing(formValue => KeyFactory[TimerKey](formValue), "error.url", Nil)(key, data)
//
//    override def unbind(key: String, value: TimerKey): Map[String, String] = Map(key -> value.toString)
//  }

//  implicit object LogicAlarmKeyFormatter extends Formatter[LogicAlarmKey] {
//    override val format: Option[(String, Nil.type)] = Some(("format.LogicAlarmKey", Nil))
//
//    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LogicAlarmKey] =
//      parsing(formValue => KeyFactory[LogicAlarmKey](formValue), "error.url", Nil)(key, data)
//
//    override def unbind(key: String, value: LogicAlarmKey): Map[String, String] = Map(key -> value.toString)
//  }

//  implicit object ModeFormatter extends Formatter[Mode] {
//    override val format: Option[(String, Nil.type)] = Some(("format.Offset", Nil))
//
//    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Mode] =
//      parsing(formValue => KeyFactory[Mode](formValue), "error.offset", Nil)(key, data)
//
//    override def unbind(key: String, value: Mode): Map[String, String] = Map(key -> value.toString)
//  }



  implicit val fmKey: OFormat[Key] = Json.format[Key]
  implicit val fmtNamedKey: OFormat[NamedKey] = Json.format[NamedKey]
}


