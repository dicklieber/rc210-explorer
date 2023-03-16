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

import com.wa9nnn.util.JsonFormatUtils.javaEnumFormat
import net.wa9nnn.rc210.command._
import net.wa9nnn.rc210.data.functions.FunctionNode
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.named.{NamedData, NamedKey}
import net.wa9nnn.rc210.data.schedules.DayOfWeek.DayOfWeek
import net.wa9nnn.rc210.data.schedules.{MonthOfYear, Schedule}
import net.wa9nnn.rc210.key.KeyKindEnum.KeyKind
import play.api.libs.json._
import play.api.mvc.PathBindable

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

  val r: Regex = """([a-zA-Z]+)(\d+)?""".r


/*
  def buildKey(keyKind: KeyKindVal, number: Int): Key = {

    keyKind match {
      case KeyKind.alarmKey => AlarmKey(number)
      case KeyKind.dtmfMacroKey => DtmfMacroKey(number)
      case KeyKind.functionKey => FunctionKey(number)
      case KeyKind.macroKey => MacroKey(number)
      case KeyKind.messageMacroKey => MessageMacroKey(number)
      case KeyKind.miscKey => MiscKey()
      case KeyKind.portKey => PortKey(number)
      case KeyKind.scheduleKey => ScheduleKey(number)
      case KeyKind.wordKey => WordKey(number)
      case KeyKind.courtesyToneKey => CourtesyToneKey(number)
    }
  }
*/


/*
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
      case "dtmfMacro" =>
        DtmfMacroKey(number)
      case "misc" =>
        MiscKey()
    }
  }
*/


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
    val r(kind, sNnumber) = string
    val number: Int = Option(sNnumber).map(_.toInt).getOrElse(0)
    KeyKindEnum.createKey(kind, number)
  }


  implicit def keyPathBinder(implicit intBinder: PathBindable[Key]): PathBindable[Key] = new PathBindable[Key] {
    override def bind(key: String, fromPath: String): Either[String, Key] = {
      try {
        Right(parseString(fromPath))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }
    }

    override def unbind(key: String, rcKey: Key): String =
      rcKey.toString
  }

  implicit def keyKindPathBinder(implicit intBinder: PathBindable[KeyKind]): PathBindable[KeyKind] = new PathBindable[KeyKind] {
    override def bind(key: String, fromPath: String): Either[String, KeyKind] = {
      try {
        Right(KeyKindEnum.apply(fromPath))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }
    }

    override def unbind(key: String, rcKey: KeyKind): String =
      rcKey.prettyName
  }


  implicit val fmtItemValue: OFormat[ItemValue] = Json.format[ItemValue]


  implicit val fmtNamed: OFormat[NamedKey] = Json.format[NamedKey]
  implicit val fmtNamedData: OFormat[NamedData] = Json.format[NamedData]


  implicit val fmtSchedule: OFormat[Schedule] = Json.format[Schedule]

  implicit val fmtMacro: OFormat[MacroNode] = Json.format[MacroNode]



}
