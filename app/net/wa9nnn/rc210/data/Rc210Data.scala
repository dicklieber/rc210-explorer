package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.command.ItemValue
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.data.vocabulary.MessageMacroNode
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.{MacroKey, MessageMacroKey}

case class Rc210Data(itemValues: Seq[ItemValue] = Seq.empty,
                     macros: Seq[MacroNode] = Seq.empty,
                     schedules: Seq[Schedule] = Seq.empty,
                     messageMacros: Seq[MessageMacroNode] = Seq.empty) extends LazyLogging {

  val enabledTriggers: Seq[TriggerNode] = {
    schedules
      .filter(_.nodeEnabled)
  }

  def triggers(macroKey: MacroKey): Seq[TriggerNode] = {
    val scheds = schedules.filter(_.triggerEnabled)
    val macroDtmfs = macros.filter(_.triggerEnabled)
    //todo There are other TriggerNodes
   val potentialTriggers: Seq[TriggerNode] = scheds ++: macroDtmfs
   val filteredforMacro =  potentialTriggers.filter(candidate =>
        candidate.macroToRun == macroKey)

    logger.trace("Looking for triggers for {}", macroKey)
    filteredforMacro.foreach { t =>
      logger.trace("\tfound: {} {}", t.key, t)
    }
    filteredforMacro
  }

  lazy val messageMacroMap: Map[MessageMacroKey, MessageMacroNode] = messageMacros.map(mm => mm.key -> mm).toMap

  def messageMacro(messageMacroKey: MessageMacroKey): Option[MessageMacroNode] = messageMacroMap.get(messageMacroKey)
}


