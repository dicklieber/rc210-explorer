package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.ItemValue.Values
import play.api.libs.json.{Json, OFormat}
import CommandFormats._
import scala.util.{Failure, Success, Try}

/**
 *
 * @param values                 as parsed or inputted.
 * @param maybeVIndex            used when there is more than one [[ItemValue]].
 * @param maybeMessage           if there was an error porsing or from processing the form value. This can be localized.
 */
case class ItemValue(commandId: Command, values: Values, maybeVIndex: Option[VIndex] = None, maybeMessage: Option[L10NMessage] = None) extends Ordered[ItemValue] {

  def head: String = values.headOption.getOrElse("?")

  def withVIndex(vIndex: VIndex): ItemValue = copy(maybeVIndex = Option(vIndex))
  def withError(l10NMessage:L10NMessage):ItemValue = copy(maybeMessage = Option(l10NMessage))

  override def toString: String = {
    val sVIn = maybeVIndex.map(p => s" vIndxe: $p").getOrElse("")
    val sError = maybeMessage.map(e => s" error: $maybeMessage").getOrElse("")
    s"commandId: $commandId value: ${values.mkString(", ")}$sVIn$sError)"
  }

  override def compare(that: ItemValue): Int = commandId compareTo that.commandId
}

object ItemValue {
  type Values = Seq[String]

  def apply(commandId: Command, value: String): ItemValue = {
    new ItemValue(commandId, Seq(value))
  }

  def apply(commandId: Command, triedValues: Try[Values]): ItemValue = {
    triedValues match {
      case Failure(exception) =>
        exception match {
          case L10NParseException(l10NError) =>
            new ItemValue(commandId, Seq("?"), maybeMessage = Option(l10NError))
          case e =>
            new ItemValue(commandId, Seq("?"), maybeMessage = Option(L10NMessage(e.getMessage)))
        }
      case Success(value) =>
        new ItemValue(commandId, value)
    }
  }

  implicit val fmtL10NError: OFormat[L10NMessage] = Json.format[L10NMessage]
  implicit val fmtItemValue: OFormat[ItemValue] = Json.format[ItemValue]
}



