package net.wa9nnn.rc210.model

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.key.MacroKey
import net.wa9nnn.rc210.util.MacroSelectField

trait Node extends RowSource {
  val key: Key
  def nodeEnabled:Boolean = true
}

/**
 * A [[Node]] that can invoke a Macro
 */
trait TriggerNode extends Node with RowSource {
def canRunMacro(macroKey: MacroKey):Boolean
}

//case class TriggerDetail(key:Key, macroToRun: MacroKey, description:String)
object TriggerNode {
  def header(count:Int):Header= Header(s"Trigger ($count)", "Key", "Description")
}

