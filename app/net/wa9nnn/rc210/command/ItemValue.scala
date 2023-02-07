package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.algorithm.RenderAlgorithm.Render
import net.wa9nnn.rc210.serial.Slice
import play.api.libs.json.{Json, OFormat}

/**
 *
 * @param commandId      of this item.
 * @param value          as parsed or inputted.
 * @param renderAlgo     a parsed or entered.
 */
case class ItemValue(commandId: CommandId, value: String, renderAlgo: Render) extends Ordered[ItemValue] {

  override def toString: String = {
    s"commandId: $commandId value: $value  as: $renderAlgo"
  }

  override def compare(that: ItemValue): Int = commandId compareTo that.commandId
}

object ItemValue {
  implicit val fmtItemValue: OFormat[ItemValue] = Json.format[ItemValue]
  implicit val ipFmt: OFormat[ItemError] = Json.format[ItemError]
}


case class ItemError(commandId: CommandId, problem: String, slice: Slice = Slice())

