package net.wa9nnn.rc210.model

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.bubble.{D3Link, D3Node, NodeId}
import net.wa9nnn.rc210.model.macros.MacroNodeId

import scala.util.Try


case class Schedule(nodeId:ScheduleNodeId, macroToRun: MacroNodeId, week: Int, dayOfWeek: Int, monthToRun: Int, monthly: Boolean, hours: Int, minutes: Int)
  extends RowSource with Node {

  override def toString: String = s"setPoStringNumber: $nodeId macro:$macroToRun hours: $hours"

  override def toRow: Row = {
    Row(nodeId.toString, macroToRun, week, monthToRun, monthly, hours, minutes)
  }

  /**
   * What this node can invoke.
   */
  override val outGoing: Seq[NodeId] = Seq(macroToRun)

  override def d3Node: D3Node = D3Node(nodeId, "//todo", List(D3Link(nodeId, macroToRun)))
}

object Schedule extends LazyLogging {
  val header: Header = Header("Schedules", "SetPoint", "MacroToRun", "Week", "MonthToRun", "Monthly", "Hours", "Minutes")

  private def buildSchdule(setPoint: Int, items: Seq[DataItem]): Try[Schedule] = {
    {
      val valueMap: Map[String, DataItem] = items.map { dataItem =>
        dataItem.name -> dataItem
      }.toMap

      def v(name: String): Int = {
        val dataItem: DataItem = valueMap(name)
        dataItem.value.toInt
      }

      Try {
        val schedule = new Schedule(ScheduleNodeId(setPoint),
          MacroNodeId(v("MacroToRun")),
          v("Week"),
          v("DOW"),
          v("MonthToRun"),
          v("Monthly") != 0,
          v("Hours"),
          v("Minutes"))

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

  def extractSchedules(datFile: DatFile): Seq[Schedule] = {
    for {
      pair: (Option[Int], Seq[DataItem]) <- datFile.section("Scheduler").dataItems
        .groupBy(_.maybeInt)
        .toSeq
        .sortBy(_._1)
      if pair._1.isDefined // has number
      schedule <- Schedule.buildSchdule(pair._1.get, pair._2).toOption
    } yield {
      schedule
    }
  }

}

case class ScheduleNodeId(override val number:Int) extends NodeId('s', number)
