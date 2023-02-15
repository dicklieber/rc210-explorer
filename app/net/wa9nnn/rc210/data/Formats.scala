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

import com.wa9nnn.util.JsonFormatUtils.javaEnumFormat
import controllers.BubbleFlowData
import net.wa9nnn.rc210._
import net.wa9nnn.rc210.command.{Command, ItemValue, L10NMessage, Locus, ValueType}
import net.wa9nnn.rc210.data.functions.Function
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.schedules.{DayOfWeek, MonthOfYear, Schedule}
import net.wa9nnn.rc210.model.TriggerDetail
import play.api.libs.json._

import scala.util.matching.Regex

object Formats {

  implicit val fmtFunction: Format[Function] = new Format[Function] {
    override def reads(json: JsValue): JsResult[Function] = {

      try {
        val jsKey: Key = (json \ "key").as[Key]

        val sdesc: String = (json \ "description").as[String]
        val sdest: Option[Key] = (json \ "destination").asOpt[Key]

        val f = Function(jsKey.asInstanceOf[FunctionKey], sdesc, sdest)
        JsSuccess(f)
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(key: Function): JsValue = {
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
      case "function" =>
        FunctionKey(number)
      case "word" =>
        WordKey(number)
    }
  }

  implicit val fmtMacroKey: Format[MacroKey] =  new Format[MacroKey]{
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

  implicit val fmtMessageMacroKey: OFormat[MessageMacroKey] = Json.format[MessageMacroKey]
  implicit val fmtFunctionKey: OFormat[FunctionKey] = Json.format[FunctionKey]
  implicit val fmtScheduleKey: OFormat[ScheduleKey] = Json.format[ScheduleKey]
  implicit val fmtWordKey: OFormat[WordKey] = Json.format[WordKey]

  implicit val fmtKey: Format[Key] = new Format[Key] {
    override def reads(json: JsValue): JsResult[Key] = {
      keyCommon(json)
    }

    override def writes(key: Key): JsValue = {
      JsString(key.toString)
    }
  }

  private def keyCommon(json: JsValue) = {
    try {
      val r(kind, number) = json.as[String]
      JsSuccess(buildKey(kind, number.toInt))
    }
    catch {
      case e: IllegalArgumentException => JsError(e.getMessage)
    }
  }
  implicit val fmtItemValue: OFormat[ItemValue] = Json.format[ItemValue]


  implicit val fmtNamed: OFormat[Named] = Json.format[Named]


  implicit val fmtTriggerDetail: OFormat[TriggerDetail] = Json.format[TriggerDetail]
  implicit val fmtDOW: Format[DayOfWeek] = javaEnumFormat[DayOfWeek]
  implicit val fmtMOY: Format[MonthOfYear] = javaEnumFormat[MonthOfYear]

  implicit val fmtSchedule: OFormat[Schedule] = Json.format[Schedule]

  implicit val fmtMacro: OFormat[MacroNode] = Json.format[MacroNode]

  implicit val fmtBubbleFlowData: OFormat[BubbleFlowData] = Json.format[BubbleFlowData]


  implicit val fmtMetadata: OFormat[Metadata] = Json.format[Metadata]
  implicit val fmtRc210Data: OFormat[Rc210Data] = Json.format[Rc210Data]

}