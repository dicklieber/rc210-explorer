package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.command.VIndex.kinds
import play.api.libs.json.{Json, OFormat}

/**
 * Used to identify [[ItemValue]] with Macros, Schedules, Ports, etc.
 * This is the key for Named items.
 * i * can be be port prefix or macro number after the base in command string.
 *
 * @param index 1-based 0 not used.
 */
case class VIndex(kind: String, index: Int = 0) {
  assert(kinds.contains(kind), "Add kind to kinds!")

  override def toString: String = s"$kind$index"

  def next: VIndex = copy(index = index + 1)
}

object VIndex {
  def port(index: Int): VIndex = {
    assert(index <= 3, "Macro numbers are 1 through 3")
    VIndex(kinds.head, index)
  }

  def `macro`(index: Int): VIndex = {
    assert(index <= 90, "Macro numbers are 1 through 90")
    VIndex(kinds(1), index)
  }

  def schedule(index: Int): VIndex = {
    assert(index <= 40, "Schedule numbers are 1 through 40")
    VIndex(kinds(2), index)
  }


  private val kinds = Seq("port", "macro", "schedule", "unlock")
  implicit val fmtQuantifier: OFormat[VIndex] = Json.format[VIndex]
}



