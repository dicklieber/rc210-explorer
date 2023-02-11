package net.wa9nnn.rc210.command

import com.wa9nnn.util.tableui.{Cell, CellProvider}
import play.api.libs.json._

import scala.util.matching.Regex

/**
 *
 * @param kind  e.g. port, schedule, macro.
 * @param index 1 to N
 */
sealed abstract class Key(kind: String, index: Int) extends CellProvider {
  override def toString: String = s"$kind$index"

  override def toCell: Cell = Cell(toString).withCssClass(kind)
}

case class PortKey(index: Int) extends Key("port", index) {
  assert(index <= 3, "Port numbers are 1 through 3")
}

case class AlarmKey(index: Int) extends Key("alarm", index) {
  assert(index <= 5, "Alarm numbers are 1 through 5")
}

case class MacroKey(index: Int) extends Key("macro", index) {
  assert(index <= 90, "Macro numbers are 1 through 90")
}

case class MessageMacroKey(index: Int) extends Key("messageMacro", index) {
  assert(index <= 90, "MessageMacro numbers are 1 through 70")
}

case class FunctionKey(index: Int) extends Key("function", index) {
  assert(index <= 1005, "Function numbers are 1 through 1005 ")
}

case class ScheduleKey(index: Int) extends Key("schedule", index) {
  assert(index <= 3, "Schedule numbers are 1 through 40")
}


object Key {
  val r: Regex = """([a-z]+)(\d+)""".r

  def apply(kind: String, number: Int): Key = {
    kind match {
      case "port" => PortKey(number)
      case "alarm" => AlarmKey(number)
      case "macro" => MacroKey(number)
      case "messageMacro" => MessageMacroKey(number)
      case "function" => FunctionKey(number)
    }
  }

  implicit val fmtMacroKey: OFormat[MacroKey] = Json.format[MacroKey]
  implicit val fmtMessageMacroKey: OFormat[MessageMacroKey] = Json.format[MessageMacroKey]
  implicit val fmtFunctionKey: OFormat[FunctionKey] = Json.format[FunctionKey]
  implicit val fmtScheduleKey: OFormat[ScheduleKey] = Json.format[ScheduleKey]

  implicit val fmtKey: Format[Key] = new Format[Key] {
    override def reads(json: JsValue): JsResult[Key] = {

      try {
        val r(kind, number) = json.toString()
        JsSuccess(Key(kind, number.toInt))
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(key: Key): JsValue = {
      JsString(key.toString)
    }
  }

}

case class Named(key:Key, name:String)

object  Named {
  implicit val fmtNamed: OFormat[Named] = Json.format[Named]
}


