package net.wa9nnn.rc210.things

import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import net.wa9nnn.rc210.serial.{Memory, MemorySlice, Slicer}
import net.wa9nnn.rc210.things.Commands.Command

import java.nio.file.Paths

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
  def parse(memory: Memory): CommandValue = {
    parser(memory(slice), command)
  }
}


object Commands {
  type Command = String
  val slicer = new Slicer()
  /**
   * Specify each [[CommandItem]] in order as erturned in [[Memory]].
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

case class CommandValue(command: Command, data: String)


object Test extends App {
  val memory = Memory(Paths.get("/Users/dlieber/dev/ham/rc210-explorer/resources/MemExample.txt")).get
  private val commandItem: CommandItem = CommandItem(DtmfParser, "SitePrefix", "*2108", MemorySlice(0, 4))
  private val commandValue: CommandValue = commandItem.parse(memory)
  println(commandValue)
}

sealed trait Parser {

  def apply(slice: Array[Int], command: Command): CommandValue
}

/**
 * Parse ASCII values into a string.
 */
object DtmfParser extends Parser {
  def apply(slice: Array[Int], command: Command): CommandValue = {
    val s = new String(
      slice
        .filter(_ != 0)
        .map(_.toChar)
    )
    CommandValue(command, s)
  }
}