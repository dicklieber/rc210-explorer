package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

import scala.util.{Failure, Success, Try}

/*/**
 * Defines a command that can be sent to the RC-210.
 *
 * @param label     what to show users.
 * @param commandId PK of this and whats sent to RC-210
 * @param slice     in [[Memory]]
 * @param help      tooltip.
 */
case class CommandItem(parser: Parser, label: String, commandId: CommandId, slice: MemorySlice, help: String = "") {
  /**
   * parse the value from a slice of [[Memory]].
   *
   * @param memory all of it.
   * @return parsed from [[Memory]].
   */
  def parse(memory: Memory): Try[ItemValue] = {
    parser(memory(slice))
  }
}*/


trait CommandSpecBase extends LazyLogging {
  val name: String
  /**
   * base tht can be sent to an RC-210 with an updated value. e.g. "*5104".
   */
  val command: String
  /**
   * What part of [[Memory]] we wil use.
   */
  val slicePos: SlicePos

  def parse(slice: Slice): ParseResult

  final def parse(memory: Memory): ParseResult = {
    val slice: Slice = memory(slicePos)

    val value: ParseResult = parse(slice)
    logger.whenTraceEnabled {
      logger.trace(s"========== $name ==========")
      logger.trace(s"slicePos:\t$slicePos")
      logger.trace(s"slice:\t\t$slice")
      logger.trace("Items:")
      value.values.foreach { itemValue =>
        logger.trace(s"\t\t\t$itemValue")
      }
    }
    value
  }

  val kind: String = "string"

  //  def validate(candidate: String): Try[String] // This cant work
}

/**
 * An item consisting of a DTMF string.
 *
 * @param command   base command number
 * @param slicePos  where this item lives in [[Memory]].
 */
case class DTMFSpec(override val name: String, override val command: String, override val slicePos: SlicePos) extends CommandSpecBase with LazyLogging {
  override def parse(slice: Slice): ParseResult = {
    val triedValue: Try[String] = DtmfParser(slice)

    val commandId = CommandId(command)
    val itemValue = triedValue match {
      case Failure(exception) =>
        ItemValue(commandId, "?", Option(ItemProblem(exception.getMessage, Option(slice))))
      case Success(value) =>
        ItemValue(commandId, value)
    }
    ParseResult(slice, itemValue)
  }
}

/**
 * An item consisting of a single boolean value.
 *
 * @param command base command number
 * @param offset  into Memory
 */
case class BoolSpec(override val name: String, command: String, offset: Int) extends CommandSpecBase with LazyLogging {
  override val slicePos: SlicePos = SlicePos(offset)

  def parse(slice: Slice): ParseResult = {
    ParseResult(slice, Seq(ItemValue(CommandId(command), (slice.head != 0).toString)))
  }
}

case class Hangtime(offset: Int) extends CommandSpecBase with LazyLogging {
  override val slicePos: SlicePos = SlicePos(offset, 9)
  override val name: String = "Hangtime"
  override val command: String = "*1000"

  def parse(slice: Slice): ParseResult = {
    val iterator = slice.iterator
   val itemValues =  for {
      sub <- 0 until 3
      port <- 0 until 3
    } yield {
      ItemValue(CommandId(command, port, sub), iterator.next().toString)
    }

    ParseResult(slice, itemValues)
  }

}

case class ParseResult(slice: Slice, values: Seq[ItemValue]) {
  override def toString: String = {
    s"$slice :\n\t" +
      values.map(_.toString).mkString("\r\t")
  }
}

object ParseResult {
  def apply(slice: Slice, itemValue: ItemValue): ParseResult = {
    new ParseResult(slice, Seq(itemValue))
  }
}