package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.serial.Slice
import play.api.libs.json.{Json, OFormat}

//sealed trait ItemValue
//
//case class ItemString(value: String) extends ItemValue {
//  override def toString: String = value
//}
//
//case class ItemStrings(values: List[String]) extends ItemValue {
//  override def toString: String = values.mkString(" ")
//}
//
//case class ItemBoolean(value: Boolean) extends ItemValue {
//  override def toString: String = value.toString
//}
//

/**
 *
 * @param commandId of this item.
 * @param value     a parsed or entered.
 * @param error     if true or false handle as a boolean otherwise edit as a string.
 */
case class ItemValue(commandId: CommandId, value: String, error: Option[ItemProblem] = None) {

  override def toString: String = {
    val sProblem = error.map(problem => s" error: $problem").getOrElse("")

    s"commandId: $commandId value: $value $sProblem"
  }
}

object ItemValue {
  implicit val ivFmt: OFormat[ItemValue] = Json.format[ItemValue]
}


case class ItemProblem( problem: String, slice: Option[Slice] = None)

object ItemProblem {
  implicit val ipFmt: OFormat[ItemProblem] = Json.format[ItemProblem]
}