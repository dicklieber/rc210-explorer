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

import com.wa9nnn.util.JsonFormatUtils.javaEnumFormat
import net.wa9nnn.rc210.command._
import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.data.functions.FunctionNode
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.named.{NamedData, NamedKey}
import net.wa9nnn.rc210.data.schedules.{DayOfWeek, MonthOfYear, Schedule}
import net.wa9nnn.rc210.data.vocabulary.MessageMacroNode
import play.api.libs.json._

import scala.util.Try
import scala.util.matching.Regex

object KeyFormats {

  implicit val fmtFunction: Format[FunctionNode] = new Format[FunctionNode] {
    override def reads(json: JsValue): JsResult[FunctionNode] = {

      try {
        val jsKey: Key = (json \ "key").as[Key]

        val sdesc: String = (json \ "description").as[String]
        val sdest: Option[Key] = (json \ "destination").asOpt[Key]

        val f = FunctionNode(jsKey.asInstanceOf[FunctionKey], sdesc, sdest)
        JsSuccess(f)
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(key: FunctionNode): JsValue = {
      JsString(key.toString)
    }
  }

  val r: Regex = """([a-zA-Z]+)(\d+)""".r

  def buildKey(kind: String, number: Int): Key = {
    kind match {
      case "port" =>
        PortKey(number)
      case "alarm" =>
        AlarmKey(number)
      case "macro" =>
        MacroKey(number)
      case "messageMacro" =>
        MessageMacroKey(number)
      case "schedule" =>
        ScheduleKey(number)
      case "function" =>
        FunctionKey(number)
      case "word" =>
        WordKey(number)
      case "dtmf" =>
        DtmfMacroKey(number)
    }
  }

  implicit val fmtMacroKey: Format[MacroKey] = new Format[MacroKey] {
    def writes(key: MacroKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue) = {
      throw new NotImplementedError() //todo
    }
  }
  implicit val fmtL10NError: OFormat[L10NMessage] = Json.format[L10NMessage]
  implicit val fmtCommandId: Format[Command] = javaEnumFormat[Command]


  implicit val fmtLocus: Format[Locus] = javaEnumFormat[Locus]
  implicit val fmtValueType: Format[ValueType] = javaEnumFormat[ValueType]


  implicit val fmtFunctionKey: Format[FunctionKey] = new Format[FunctionKey] {
    def writes(key: FunctionKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue) = {
      throw new NotImplementedError() //todo
    }
  }


  implicit val fmtScheduleKey: Format[ScheduleKey] = new Format[ScheduleKey] {
    def writes(key: ScheduleKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue) = {
      throw new NotImplementedError() //todo
    }
  }
  implicit val fmtMessageMacroKey: Format[MessageMacroKey] = new Format[MessageMacroKey] {
    def writes(key: MessageMacroKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue) = {
      throw new NotImplementedError() //todo
    }
  }
  implicit val fmtWordKey: Format[WordKey] = new Format[WordKey] {
    def writes(key: WordKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue) = {
      throw new NotImplementedError() //todo
    }
  }


  implicit val fmtKey: Format[Key] = new Format[Key] {
    override def reads(json: JsValue): JsResult[Key] = {
      JsResult.fromTry(Try {
        KeyFormats.parseString(json.as[String])
      })
    }

    override def writes(key: Key): JsValue = {
      JsString(key.toString)
    }
  }

  def parseString(string: String): Key = {
    val r(kind, number) = string
    buildKey(kind, number.toInt)
  }

  private def keyConetwmmon(json: JsValue) = {
    try {
      val key = parseString(json.as[String])
      JsSuccess(key)
    }
    catch {
      case e: IllegalArgumentException => JsError(e.getMessage)
    }
  }

  implicit val fmtItemValue: OFormat[ItemValue] = Json.format[ItemValue]


  implicit val fmtNamed: OFormat[NamedKey] = Json.format[NamedKey]
  implicit val fmtNamedData: OFormat[NamedData] = Json.format[NamedData]

  implicit val fmtDOW: Format[DayOfWeek] = javaEnumFormat[DayOfWeek]
  implicit val fmtMOY: Format[MonthOfYear] = javaEnumFormat[MonthOfYear]

  implicit val fmtSchedule: OFormat[Schedule] = Json.format[Schedule]

  implicit val fmtMacro: OFormat[MacroNode] = Json.format[MacroNode]
  implicit val fmtMessageMacro: OFormat[MessageMacroNode] = Json.format[MessageMacroNode]


  implicit val fmtRc210Data: OFormat[Rc210Data] = Json.format[Rc210Data]

}
