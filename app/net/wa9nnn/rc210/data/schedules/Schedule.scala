package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.JsonFormatUtils.*
import com.wa9nnn.util.tableui.{Cell, Header, Row}
import net.wa9nnn.rc210.data.TriggerNode
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.data.field.schedule.{DayOfWeek, Week}
import net.wa9nnn.rc210.data.schedules.Schedule.s02
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.Display
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.*

import java.time.LocalTime

/**
 *
 * @param key                    for [[ScheduleKey]]
 * @param dow                    [[DayOfWeek]] or [[WeekAndDow]].
 * @param monthOfYear            enumerated
 * @param hour                   when this runs on selected day.
 * @param minute                 when this runs on selected day.
 * @param macroKey               e.g. "macro42"
 * @param enabled                duh
 */
case class Schedule(override val key: Key,
                    dow: DayOfWeek = DayOfWeek.EveryDay,
                    week: Week = Week.Every,
                    monthOfYear: MonthOfYearSchedule = MonthOfYearSchedule.Every,
                    hour: Int = 0,
                    minute: Int = 0,
                    macroKey: Key = Key(KeyKind.RcMacro, 1),
                    enabled: Boolean = false) extends ComplexFieldValue() with TriggerNode  {

  val description: String = {
    //    val week = s" Week: $weekInMonth"
    //    s"$monthOfYear$week on $dayOfWeek at $time"
    "" //todo"
  }
  
  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val setPoint: String = key.rc210Value.toString
    val sDow: String = dow.rc210Value.toString
    val sWeek: String = week match {
      case Week.Every =>
        ""
      case d: Week =>
        d.rc210Value.toString
    }
    val moy: String = s02(monthOfYear.rc210Value)
    val hours: String = s02(hour)
    val minutes: String = s02(minute)
    val sMacro = s02(macroKey.rc210Value)

    val command = s"1*4001$setPoint*$sWeek$sDow*${moy}*$hours*$minutes*$sMacro"
    Seq(command)
  }

  override def displayHtml: String =
    <table>
      <tr>
        <td>Day Of Week</td>
        <td>
          {dow}
        </td>
      </tr>
      <tr>
        <td>Week</td>
        <td>
          {week}
        </td>
      </tr>
      <tr>
        <td>Month</td>
        <td>
          {monthOfYear}
        </td>
      </tr>
      <tr>
        <td>Hour</td>
        <td>
          {hour}
        </td>
      </tr>
      <tr>
        <td>Minute</td>
        <td>
          {minute}
        </td>
      </tr>
      <tr>
        <td>Macro</td>
        <td>
          {macroKey}
        </td>
      </tr>
      <tr>
        <td>Enabled</td>
        <td>
          {Display(enabled)}
        </td>
      </tr>
    </table>
      .toString


  override def toJsValue: JsValue = Json.toJson(this)

  override def canRunMacro(candidate: Key): Boolean = macroKey == candidate
}

object Schedule extends LazyLogging with ComplexExtractor[Schedule] {
  override val keyKind: KeyKind = KeyKind.Schedule
  def unapply(schedule: Schedule): Option[(Key, DayOfWeek, Week, MonthOfYearSchedule, Int, Int, Key, Boolean)] =
    Some(schedule.key, schedule.dow, schedule.week, schedule.monthOfYear, schedule.hour, schedule.minute, schedule.macroKey, schedule.enabled)

  override val form: Form[Schedule] = Form[Schedule](
    mapping(
      "key" -> of[Key],
      "dow" -> DayOfWeek.formField,
      "week" -> Week.formField,
      "monthOfYear" -> MonthOfYearSchedule.formField,
      "hour" -> number(0, 23),
      "minute" -> number(0, 59),
      "macroKey" -> of[Key],
      "enabled" -> boolean
    )(Schedule.apply)(Schedule.unapply)
  )

  /**
   * Format as a 2digit inte.g. 2 become "02"
   */
  def s02(n: Int): String = f"$n%02d"

  def empty(setPoint: Int): Schedule = {
    val scheduleKey: Key = Key(KeyKind.Schedule, setPoint)
    new Schedule(
      key = scheduleKey
    )
  }

  val header = Header.singleRow(
    "SetPoint",
    "Enabled",
    "Day in Week",
    "Month",
    Cell("Week").withToolTip("Week in month"),
    "Time",
    "Macro To Run")

  override def positions: Seq[FieldOffset] = {
    Seq(
      FieldOffset(616, this)
    )
  }

  override def extract(memory: Memory): Seq[FieldEntry] = {
    ScheduleBuilder(memory).map { schedule =>
      FieldEntry(this, FieldKey("Schedule", schedule.key), schedule)
    }
  }


  def apply(setPoint: Int): Schedule = new Schedule(Key(KeyKind.Schedule, setPoint))


  implicit val fmtSchedule: Format[Schedule] = Json.format[Schedule]


  override def parse(jsValue: JsValue): FieldValue = jsValue.as[Schedule]

  override val name: String = "Schedule"
  override val fieldName: String = name

}





