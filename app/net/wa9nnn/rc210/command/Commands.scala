package net.wa9nnn.rc210.command

import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.{Memory, MemorySlice, Slicer}
import net.wa9nnn.rc210.command.Commands.Command

import java.nio.file.Paths
import scala.util.{Failure, Success, Try}

object Commands extends LazyLogging {
  /**
   * Build RC-210 State
   * This extracts all the [[CommandValue]]s from a [[Memory]].
   *
   * @param memory data freom an RC=210.
   * @return
   */
  def parse(memory: Memory): Map[Command, Try[ItemValue]] = {
    commands.map { ci: CommandItem =>
      ci.command -> ci.parse(memory)
    }.toMap
  }

  type Command = String
  val slicer = new Slicer()
  /**
   * Specify each [[CommandItem]] in order as returned in [[Memory]].
   */
  val commands = List(
    CommandItem(DtmfParser, "SitePrefix", "*2108", slicer(4)),
    CommandItem(DtmfParser, "TTPadTest", "*2093", slicer(6))
  )

  val commandMap: Map[Command, CommandItem] = commands
    .map(c => c.command -> c)
    .toMap
  val valueMap = new TrieMap[Command, CommandValue]() // mutable
}

case class CommandValue(command: Command, data: Try[ItemValue])

case class ItemString(value: String) extends ItemValue {
  override def toString: String = value
}

case class ItemStrings(values: List[String]) extends ItemValue {
  override def toString: String = values.mkString(" ")
}

case class ItemBoolean(value: Boolean) extends ItemValue {
  override def toString: String = value.toString
}

trait ItemValue