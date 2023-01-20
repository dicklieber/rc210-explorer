package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.model.DataItem

import scala.util.Try


case class Schedule(setPoStringNumber: Int, macroToRun: Int, week: Int, dayOfWeek: Int, monthToRun: Int, monthly: Boolean, hours: Int, minutes: Int){
  override def toString: String = s"setPoStringNumber: $setPoStringNumber macro:$macroToRun hours: $hours"
}

object Schedule extends LazyLogging {
  def buildSchdule(setPoint: Int, items: Seq[DataItem]): Try[Schedule] = {
    {
      val valueMap: Map[String, DataItem] = items.map { dataItem =>
        dataItem.name -> dataItem
      }.toMap

      def v(name: String): Int = {
        val dataItem: DataItem = valueMap(name)
        dataItem.value.toInt
      }

      Try {
        val schedule = new Schedule(setPoint,
          v("MacroToRun"),
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
}