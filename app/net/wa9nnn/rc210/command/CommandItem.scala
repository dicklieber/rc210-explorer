package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.command.algorithm.RenderAlgorithm._
import net.wa9nnn.rc210.serial.{Memory, Slice, SlicePos}

import scala.util.{Failure, Success}

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

  def parse(slice: Slice): ParseSliceResult

  final def parse(memory: Memory): ParseSliceResult = {
    val slice: Slice = memory(slicePos)

    val value: ParseSliceResult = parse(slice)
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
case class DTMF(override val name: String, override val command: String, override val slicePos: SlicePos) extends CommandSpecBase with LazyLogging {
  override def parse(slice: Slice): ParseSliceResult = {
    val commandId = CommandId(command)
    val builder = ParseResultBuilder(slice)

    DtmfParser(slice) match {
      case Failure(exception) =>
        builder(ItemError(commandId, exception.getMessage, slice))
        builder(ItemValue(commandId, "?", dtmf))
      case Success(value) =>
        builder(ItemValue(commandId, value, dtmf))
    }
    builder.result
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

  def parse(slice: Slice): ParseSliceResult = {
    val builder = ParseResultBuilder(slice)
    builder(ItemValue(CommandId(command), (slice.head != 0).toString, bool))
    builder.result
  }
}

/**
 * An item consisting of a single boolean value.
 *
 * @param command base command number
 * @param offset  into Memory
 */

case class IntValue(override val name: String, command: String, offset: Int) extends CommandSpecBase with LazyLogging {
  override val slicePos: SlicePos = SlicePos(offset)

  def parse(slice: Slice): ParseSliceResult = {
    val builder = ParseResultBuilder(slice)
    val commandId = CommandId(command)
    builder(ItemValue(commandId, (slice.head.toString), int8))
    builder.result
  }
}

/**
 * An item consisting of a single 16 bit integer.
 *
 * @param command base command number
 * @param offset  into Memory
 */

case class IntValue16(override val name: String, command: String, offset: Int) extends CommandSpecBase with LazyLogging {
  override val slicePos: SlicePos = SlicePos(offset, 2)

  def parse(slice: Slice): ParseSliceResult = {
    val builder = ParseResultBuilder(slice)
    val value: Int = slice.head + slice.data(1) * 256
    val commandId = CommandId(command)
    builder(ItemValue(commandId, (value.toString), int16))
    builder.result
  }
}

case class Hangtime(offset: Int) extends CommandSpecBase with LazyLogging {
  override val slicePos: SlicePos = SlicePos(offset, 9)
  override val name: String = "Hangtime"
  override val command: String = "*1000"

  def parse(slice: Slice): ParseSliceResult = {
    val builder = ParseResultBuilder(slice)
    val iterator = slice.iterator
    for {
      sub <- 0 until 3
      port <- 0 until 3
    } {
      builder(ItemValue(CommandId(command, port, sub), iterator.next().toString, int8))
    }
    builder.result
  }
}

case class PortInts(override val name: String, command: String, offset: Int) extends CommandSpecBase with LazyLogging {
  override val slicePos: SlicePos = SlicePos(offset, 3)

  def parse(slice: Slice): ParseSliceResult = {
    val builder = ParseResultBuilder(slice)
    val iterator = slice.iterator
    for {
      port <- 0 until 3
    } {
      builder(ItemValue(CommandId(command, port), iterator.next().toString, int8))
    }
    builder.result
  }
}

case class PortInts16(override val name: String, command: String, offset: Int) extends CommandSpecBase with LazyLogging {
  override val slicePos: SlicePos = SlicePos(offset, 6)

  def parse(slice: Slice): ParseSliceResult = {
    val builder = ParseResultBuilder(slice)

    val iterator = slice.iterator
    for {
      port <- 0 until 3
    } {
      val number = iterator.next() + iterator.next() * 256
      builder(ItemValue(CommandId(command, port), number.toString, int16))
    }
    builder.result
  }
}

/*
case class SubSet(override val name: String, command: String, offset: Int) extends CommandSpecBase with LazyLogging {
  override val slicePos: SlicePos = SlicePos(offset, 4)

  def parse(slice: Slice): ParseSliceResult = {
    val psr = ParseSliceResult(slice)
    val iterator = slice.iterator
    val itemValues = for {
      port <- 0 until 2
    } yield {
      val number = iterator.next() + iterator.next() * 256
      ItemValue(CommandId(command, port), number.toString, range)
    }
    ParseSliceResult(slice, itemValues)
  }
}
*/

