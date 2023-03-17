package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.MemoryExtractor
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.data.schedules.DayOfWeek.DayOfWeek
import net.wa9nnn.rc210.data.schedules.MonthOfYear.MonthOfYear
import net.wa9nnn.rc210.key.KeyKindEnum._
import net.wa9nnn.rc210.key.{Key, MacroKey, ScheduleKey}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import play.api.libs.json.{JsString, JsValue}

import java.time.LocalTime

/**
 *
 * @param key         for [[ScheduleKey]]
 * @param dayOfWeek    See [[DayOfWeekJava]]
 * @param weekInMonth  e.g 1 == 1st week in month.
 * @param monthOfYear  See [[MonthOfYear]]
 * @param localTime    illegal times are None.
 * @param macroToRun   e.g. "macro42"
 */
case class Schedule(key: ScheduleKey, dayOfWeek: DayOfWeek, weekInMonth: Option[Int], monthOfYear: MonthOfYear, localTime: Option[LocalTime], macroToRun: MacroKey)
  extends FieldContents with TriggerNode {


  override def toRow: Row = {
    Row(key.toCell, macroToRun.toCell, dayOfWeek, weekInMonth, monthOfYear, localTime)
  }

  override val nodeEnabled: Boolean = localTime.nonEmpty

  val description: String = {
    localTime.map { localTime =>
      val week = weekInMonth.map { week =>
        s" Week: $week"
      }.getOrElse("")
      s"$monthOfYear$week on $dayOfWeek at $localTime"
    }
      .getOrElse("-disabled-")
  }

  override def triggerRow: Row = {
    Row(key.toCell, description)
  }


  override val triggerEnabled: Boolean = nodeEnabled

  override def triggerDescription: String = toString

  override def toJsValue: JsValue = JsString(description) //todo JsObject of Schedule.

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  /**
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   * @param fieldEntry all the metadata.
   * @return html
   */
  override def toHtmlField(fieldEntry: FieldEntry): String =
    views.html.schedule(this, scheduleKey).toString()
}

object Schedule extends LazyLogging with MemoryExtractor  {
  def header(count: Int): Header = Header(s"Schedules ($count)", "SetPoint", "Macro", "DOW", "WeekInMonth", "MonthOfYear", "LocalTime")

  override def extract(memory: Memory): Seq[FieldEntry] = {

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
    for (setPoint <- 0 until 40) yield {

      val parts: Seq[Any] = scheduleBuilder.getSetpointRow(setPoint)


      //*4001 S * DOW * MOY * Hours * Minutes * Macro

      val key = ScheduleKey(setPoint + 1)
      val schedule = Schedule(key, dayOfWeek = DayOfWeek(parts(0).asInstanceOf[Int]), weekInMonth = parts(1).asInstanceOf[Option[Int]], monthOfYear = parts(2).asInstanceOf[MonthOfYear], localTime = {
        val hour: Int = parts(3).asInstanceOf[Int]
        val minute: Int = parts(4).asInstanceOf[Int]
        try {
          Option.when(hour < 25) {
            LocalTime.of(hour, minute)
          }
        } catch {
          case _: Exception =>
            logger.error(s"setPoint: $setPoint hour: $hour, minute: $minute")
            None
        }
      }, macroToRun = parts(5).asInstanceOf[MacroKey])
      FieldEntry(this, FieldKey("Schedule",key), schedule)
    }
  }

  override def prompt: String = ""

  override val fieldName: String = "Schedule"
  override val kind: KeyKind = scheduleKey
}




