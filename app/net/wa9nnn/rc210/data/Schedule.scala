package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.NumberedValues
import net.wa9nnn.rc210.bubble.{D3Link, D3Node, NodeId}
import net.wa9nnn.rc210.model.macros.MacroNodeId
import net.wa9nnn.rc210.model.{DatFile, Node}

import scala.util.Try


case class Schedule(nodeId: ScheduleNodeId, macroToRun: MacroNodeId, week: Int, dayOfWeek: Int, monthToRun: Int, monthly: Boolean, hours: Int, minutes: Int)
  extends RowSource with Node {

  override def toString: String = s"setPoStringNumber: $nodeId macro:$macroToRun hours: $hours"

  override def toRow: Row = {
    Row(nodeId.toString, macroToRun, week, monthToRun, monthly, hours, minutes)
  }

  override def d3Node: D3Node = D3Node(nodeId, s"SetPoint: ${nodeId.number} nmacroToRun: ${macroToRun.number}", List(D3Link(nodeId, macroToRun)))
}

object Schedule {
  val header: Header = Header("Schedules", "SetPoint", "MacroToRun", "Week", "MonthToRun", "Monthly", "Hours", "Minutes")
}

object Schedules extends LazyLogging {

  def apply(datFile: DatFile): List[Schedule] = {
    datFile.section("Scheduler")
      .process[Schedule] { numberedValues =>
        buildSchdule(numberedValues)
      }
  }

  private def buildSchdule(implicit numberedValues: NumberedValues): Try[Schedule] = {
    {
      import NumberedValues._
      Try {
        val setPoint = numberedValues.number.get // schedules always have a number (setpoint)
        val schedule = Schedule(
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


case class ScheduleNodeId(override val number: Int) extends NodeId('s', number)
