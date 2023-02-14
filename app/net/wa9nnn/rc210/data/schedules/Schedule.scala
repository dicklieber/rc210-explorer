package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.JsonFormatUtils.javaEnumFormat
import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import net.wa9nnn.rc210.{MacroKey, ScheduleKey}
import play.api.libs.json.{Format, Json, OFormat}

import java.time.LocalTime

/**
 *
 * @param key          e.g "schedule5"
 * @param dayOfWeek    See [[DayOfWeek]]
 * @param weekInMonthm e.g 1 == 1st week in month.
 * @param monthOfYear  See [[MonthOfYear]]
 * @param localTime    illegal times are None.
 * @param macroToRun   e.g. "macro42"
 */
case class Schedule(key: ScheduleKey,
                    dayOfWeek: DayOfWeek,
                    weekInMonth: Option[Int],
                    monthOfYear: MonthOfYear,
                    localTime: Option[LocalTime],
                    macroToRun: MacroKey)
  extends TriggerNode {


  override def toRow: Row = {
    Row(key.toCell, macroToRun.toCell, dayOfWeek, weekInMonth, monthOfYear, localTime)
  }

  override def enabled: Boolean = localTime.nonEmpty


  override def toString: String = {
    s"DOW: $dayOfWeek WeekInMonth: $weekInMonth Month: $monthOfYear time: $localTime"
  }

  override def triggerRow: Row = {
    Row(key.toCell, this)
  }
}

object Schedule {
  def header(count: Int): Header = Header(s"Schedules ($count)", "SetPoint", "Macro", "DOW", "WeekInMonth", "MonthOfYear", "LocalTime")

  implicit val fmtDOW: Format[DayOfWeek] = javaEnumFormat[DayOfWeek]
  implicit val fmtMOY: Format[MonthOfYear] = javaEnumFormat[MonthOfYear]

  implicit val fmtSchedule: OFormat[Schedule] = Json.format[Schedule]
}

object ScheduleExtractor extends LazyLogging {
  def apply(memory: Memory): Seq[Schedule] = {
    // dim0 setpoint row, dim1 is piece column


    /**
     * Wxtract one piece of a schedules.
     *
     * @param php  where in the [[Memory]]
     * @returns one int for eqch setpoint.
     */
    def collect(php: String): Seq[Int] = {
      memory(SlicePos(php)).data
    }

    val scheduleBuilder = new ScheduleBuilder()
    // Collection values from various places in Memory.
    scheduleBuilder.putDow(collect("//SetPointDOW - 616-655"))
    scheduleBuilder.putMoy(collect("//SetPointMOY - 656-695"))
    scheduleBuilder.putHours(collect("//SetPointHours - 696-735"))
    scheduleBuilder.putMinutes(collect("//SetPointMinutes - 736-775"))
    scheduleBuilder.putMacro(collect("//SetPointMacro - 776-815"))

    //DoSetPoint - 816-855
    val r: Seq[Schedule] = for (setPoint <- 0 until 40) yield {

      val parts = scheduleBuilder.getSetpointRow(setPoint)

      Schedule(
        key = ScheduleKey(setPoint),
        dayOfWeek = parts.head.asInstanceOf[DayOfWeek],
        weekInMonth = parts(1).asInstanceOf[Option[Int]],
        monthOfYear = parts(2).asInstanceOf[MonthOfYear],
        localTime = {
          val hour: Int = parts(3).asInstanceOf[Int]
          val minute: Int = parts(4).asInstanceOf[Int]
          try {
            Option.when(hour < 25) {
              LocalTime.of(hour, minute)
            }
          } catch {
            case e: Exception =>
              logger.error(s"setPoint: $setPoint hour: $hour, minute: $minute")
              None
          }
        },
        macroToRun = parts(5).asInstanceOf[MacroKey]
      )
    }
    r
  }
}





