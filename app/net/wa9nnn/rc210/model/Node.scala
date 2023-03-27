package net.wa9nnn.rc210.model

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.key.KeyFactory.{Key, MacroKey}
import net.wa9nnn.rc210.util.MacroSelect

trait Node extends RowSource {
  val key: Key
  def nodeEnabled:Boolean = true
}

/**
 * A [[Node]] that can invoke a Macro
 */
trait TriggerNode extends Node with RowSource {
  def macroToRun: MacroKey
  def triggerEnabled:Boolean
  def triggerRow:Row = Row(key.toCell, triggerDescription)
  def triggerDescription:String
}

//case class TriggerDetail(key:Key, macroToRun: MacroKey, description:String)
object TriggerNode {
  def header(count:Int):Header= Header(s"Trigger ($count)", "Key", "Description")
}

