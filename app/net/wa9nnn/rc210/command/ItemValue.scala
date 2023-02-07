package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.ItemValue.Values
import play.api.libs.json.{Json, OFormat}

import scala.util.{Failure, Success, Try}

/**
 *
 * @param commandId       of this item.
 * @param values          as parsed or inputted.
 * @param error           if there was an error porsing or from processing the form value. This can be localized.
 */
case class ItemValue(commandId: CommandId,
                     values: Values,
                     error: Option[L10NMessage] = None) extends Ordered[ItemValue] {

  def head: String = values.headOption.getOrElse("?")

  override def toString: String = {
    s"commandId: $commandId value: ${values.mkString(", ")}"
  }

  override def compare(that: ItemValue): Int = commandId compareTo that.commandId
}

object ItemValue {
  type Values = Seq[String]

  def apply(commandId: CommandId, triedValues: Try[Values]): ItemValue = {
    triedValues match {
      case Failure(exception) =>
        exception match {
          case L10NParseException(l10NError) =>
            new ItemValue(commandId, Seq("?"), Option(l10NError))
          case e =>
            new ItemValue(commandId, Seq("?"), Option(L10NMessage(e.getMessage)))
        }
      case Success(value) =>
        new ItemValue(commandId, value)
    }
  }

  import CommandFormats._

  implicit val fmtL10NError: OFormat[L10NMessage] = Json.format[L10NMessage]
  implicit val fmtItemValue: OFormat[ItemValue] = Json.format[ItemValue]
}



