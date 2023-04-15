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
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.key.KeyFactory.{CourtesyToneKey, FunctionKey, Key, MacroKey, MessageMacroKey, ScheduleKey, TimerKey, WordKey}
import play.api.libs.json._
import play.api.mvc.PathBindable

import scala.util.Try
import scala.util.matching.Regex
import scala.language.postfixOps

object KeyFormats {

  implicit val fmtFunction: Format[FunctionNode] = new Format[FunctionNode] {
    override def reads(json: JsValue): JsResult[FunctionNode] = {

      try {
        val jsKey: FunctionKey = (json \ "key").as[Key].asInstanceOf[FunctionKey]

        val sdesc: String = (json \ "description").as[String]
        val sdest: Option[Key] = (json \ "destination").asOpt[Key]

        val f = FunctionNode(jsKey, sdesc, sdest)
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


  implicit val fmtCourtesyToneKey: Format[CourtesyToneKey] = new Format[CourtesyToneKey] {
    def writes(key: CourtesyToneKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue): JsResult[CourtesyToneKey] = {
      JsSuccess(KeyFactory.apply(json.as[String]))
    }
  }

  implicit val fmtTimerKey: Format[TimerKey] = new Format[TimerKey] {
    def writes(key: TimerKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue): JsResult[TimerKey] = {
      JsSuccess(KeyFactory.apply(json.as[String]))
    }
  }


  implicit val fmtMacroKey: Format[MacroKey] = new Format[MacroKey] {
    def writes(key: MacroKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue): JsResult[MacroKey] = {
      JsSuccess(KeyFactory.apply(json.as[String]))
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

    override def reads(json: JsValue): JsResult[FunctionKey] = {
      JsSuccess(KeyFactory.apply(json.as[String]))    }
  }


  implicit val fmtScheduleKey: Format[ScheduleKey] = new Format[ScheduleKey] {
    def writes(key: ScheduleKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue): JsResult[ScheduleKey] = {
      JsSuccess(KeyFactory.apply(json.as[String]))    }
  }
  implicit val fmtMessageMacroKey: Format[MessageMacroKey] = new Format[MessageMacroKey] {
    def writes(key: MessageMacroKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue): JsResult[MessageMacroKey] = {
      JsSuccess(KeyFactory.apply(json.as[String]))    }
  }
  implicit val fmtWordKey: Format[WordKey] = new Format[WordKey] {
    def writes(key: WordKey): JsValue = {
      JsString(key.toString)
    }

    override def reads(json: JsValue): JsResult[WordKey] = {
      JsSuccess(KeyFactory.apply(json.as[String]))    }
  }


  implicit val fmtKey: Format[Key] = new Format[Key] {
    override def reads(json: JsValue): JsResult[Key] = {
      JsResult.fromTry(Try {
        KeyFactory[Key](json.as[String])
      })
    }

    override def writes(key: Key): JsValue = {
      JsString(key.toString)
    }
  }

  implicit def keyKindPathBinder(implicit intBinder: PathBindable[KeyKind]): PathBindable[KeyKind] = new PathBindable[KeyKind] {
    override def bind(key: String, fromPath: String): Either[String, KeyKind] = {
      try {
        Right(KeyKind.valueOf(fromPath))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }
    }

    override def unbind(key: String, keyKind: KeyKind): String =
      keyKind.toString
  }


  implicit val fmtItemValue: OFormat[ItemValue] = Json.format[ItemValue]


  implicit val fmtNamed: OFormat[NamedKey] = Json.format[NamedKey]
  implicit val fmtNamedData: OFormat[NamedData] = Json.format[NamedData]


  implicit val fmtMacro: OFormat[MacroNode] = Json.format[MacroNode]


}
