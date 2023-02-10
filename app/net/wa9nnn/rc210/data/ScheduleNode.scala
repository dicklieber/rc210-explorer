package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.command.{MacroKey, ScheduleKey}
import net.wa9nnn.rc210.model.TriggerNode


case class ScheduleNode(key: ScheduleKey, macroToRun: MacroKey, week: Int, dayOfWeek: Int, monthToRun: Int, monthly: Boolean, hours: Int, minutes: Int)
  extends RowSource with TriggerNode {

  override def description: String = s"setPoStringNumber: $key macro:$macroToRun hours: $hours"

  override def toRow: Row = {
    Row(key.toCell, description)
  }
}

object ScheduleNode {
  val header: Header = Header("Schedules", "SetPoint", "MacroToRun", "Week", "MonthToRun", "Monthly", "Hours", "Minutes")
}

object Schedules extends LazyLogging {


}


