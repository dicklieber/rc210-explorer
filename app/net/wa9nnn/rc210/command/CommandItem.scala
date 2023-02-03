package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.Commands.Command
import net.wa9nnn.rc210.serial.{Memory, MemorySlice}

import scala.util.Try

/**
 * Defines a command that can be sent to the RC-210.
 *
 * @param label   what to show users.
 * @param command PK of this and whats sent to RC-210
 * @param slice   in [[Memory]]
 * @param help    tooltip.
 */
case class CommandItem(parser: Parser, label: String, command: Command, slice: MemorySlice, help: String = "") {
  /**
   * parse the value from a slice of [[Memory]].
   *
   * @param memory all of it.
   * @return parsed from [[Memory]].
   */
  def parse(memory: Memory): Try[ItemValue] = {
    parser(memory(slice))
  }
}