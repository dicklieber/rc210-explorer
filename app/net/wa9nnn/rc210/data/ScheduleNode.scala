package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.NumberedValues
import net.wa9nnn.rc210.bubble.{NodeId, TriggerNode}
import net.wa9nnn.rc210.model.DatFile
import net.wa9nnn.rc210.model.macros.MacroNodeId

import scala.util.Try


case class ScheduleNode(nodeId: ScheduleNodeId, macroToRun: MacroNodeId, week: Int, dayOfWeek: Int, monthToRun: Int, monthly: Boolean, hours: Int, minutes: Int)
  extends RowSource with TriggerNode {

  override def description: String = s"setPoStringNumber: $nodeId macro:$macroToRun hours: $hours"

  override def toRow: Row = {
    Row(nodeId.toCell, description)
  }
}

object ScheduleNode {
  val header: Header = Header("Schedules", "SetPoint", "MacroToRun", "Week", "MonthToRun", "Monthly", "Hours", "Minutes")
}

object Schedules extends LazyLogging {

  def apply(datFile: DatFile): List[ScheduleNode] = {
    datFile.section("Scheduler")
      .process[ScheduleNode] { numberedValues =>
        buildSchdule(numberedValues)
      }
  }

  private def buildSchdule(implicit numberedValues: NumberedValues): Try[ScheduleNode] = {
    {
      import NumberedValues._
      Try {
        val setPoint = numberedValues.number.get // schedules always have a number (setpoint)
        val schedule = ScheduleNode(
          nodeId = ScheduleNodeId(setPoint),
          macroToRun = MacroNodeId(vi("MacroToRun")),
          dayOfWeek = vi("Week"),
          week = vi("DOW"),
          monthToRun = vi("MonthToRun"),
          monthly = vi("Monthly") != 0,
          hours = vi("Hours"),
          minutes = vi("Minutes")
        )

        logger.debug("schedule: {} ", schedule)
        val hours = schedule.hours
        if (hours < 0 || hours > 24) {
          logger.debug("\tdisabled: setPoint: {} hours: {}", setPoint, hours)
          throw new IllegalArgumentException(s"Not enabled, hours: $hours setPoint: $setPoint")
        }
        schedule
      }
    }
  }
}


case class ScheduleNodeId(override val number: Int) extends NodeId('s', number, "ScheduleNode")
