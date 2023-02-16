package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.command.ItemValue
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.data.vocabulary.MessageMacroNode
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.{MacroKey, MessageMacroKey}

case class Rc210Data(itemValues: Seq[ItemValue],
                     macros: Seq[MacroNode],
                     schedules: Seq[Schedule],
                     messageMacros: Seq[MessageMacroNode]) {

  val enabledTriggers:Seq[TriggerNode] = {
    schedules
      .filter(_.enabled)
  }
  def triggers(macroKey: MacroKey): Seq[TriggerNode] = {
    //todo There are other TriggerNodes
    schedules
      .filter(_.enabled)
  }
  lazy val messageMacroMap: Map[MessageMacroKey, MessageMacroNode] = messageMacros.map(mm => mm.key -> mm).toMap

  def messageMacro(messageMacroKey: MessageMacroKey):Option[MessageMacroNode] = messageMacroMap.get(messageMacroKey)
}


