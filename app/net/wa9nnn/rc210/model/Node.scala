package net.wa9nnn.rc210.model

import com.wa9nnn.util.tableui.RowSource
import net.wa9nnn.rc210.{Key, MacroKey}

trait Node extends RowSource {
  val key: Key
}

/**
 * A [[Node]] that can invoke a Macro
 */
trait TriggerNode extends Node {
  val macroToRun: MacroKey

  def description: String
}

