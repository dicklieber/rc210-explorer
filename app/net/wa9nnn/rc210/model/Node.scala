package net.wa9nnn.rc210.model

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.{Key, MacroKey}
import play.api.libs.json.{Json, OFormat}
import net.wa9nnn.rc210.data.Formats._

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
  def triggerDetail:TriggerDetail
}

case class TriggerDetail(key:Key, macroToRun: MacroKey, description:String)
object TriggerNode {
  def header(count:Int):Header= Header(s"Trigger ($count)", "Key", "Description")
}

