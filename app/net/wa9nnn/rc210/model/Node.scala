package net.wa9nnn.rc210.model

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.{Key, MacroKey}

trait Node extends RowSource {
  val key: Key
}

/**
 * A [[Node]] that can invoke a Macro
 */
trait TriggerNode extends Node with RowSource {
  def macroToRun: MacroKey
  def enabled:Boolean
  def triggerRow:Row
}

object TriggerNode {
  def header(count:Int):Header= Header(s"Trigger ($count)", "Key", "Description")
}

