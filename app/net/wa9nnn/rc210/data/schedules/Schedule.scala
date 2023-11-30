package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.JsonFormatUtils.*
import com.wa9nnn.util.tableui.{Cell, Header, Row}
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.TriggerNode
import net.wa9nnn.rc210.data.field.schedule.{DayOfWeek, Week}
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue, FieldEntry, FieldEntryBase, FieldKey, FieldOffset, FieldValue, MonthOfYearSchedule, RMD, RenderMetadata}
import net.wa9nnn.rc210.data.schedules.Schedule.s02
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.{Format, JsValue, Json}

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
                    macroKey: Key = Key(KeyKind.macroKey, 1),
                    enabled: Boolean = false) extends ComplexFieldValue("Schedule") with TriggerNode with RenderMetadata {

  val description: String = {
    //    val week = s" Week: $weekInMonth"
    //    s"$monthOfYear$week on $dayOfWeek at $time"
    "" //todo"
  }

  override def toRow: Row = {
    implicit val k: Key = key
    val name: Cell = k.toCell

    val localTime: Cell = {
      val h = if (hour > 24) // RC-210 use > 24 as disabled.
        0 else hour

      val localTime1 = LocalTime.of(h, minute)
      val html = views.html.fieldTime(localTime1, RMD(name = "time")).toString()
      Cell.rawHtml(html)
    }
   

    Row(
      name,
      enabled,
      dow,
      monthOfYear,
      week,
      localTime,
      macroKey.toCell
    )
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


  override def display: String = description

  override def param: String = FieldKey("Schedule", key).param

  override def prompt: String = "Runs a Macro on a "

  override def units: String = ""

  override def toJsValue: JsValue = Json.toJson(this)

  override def canRunMacro(candidate: Key): Boolean = macroKey == candidate
}

object Schedule extends LazyLogging with ComplexExtractor {
  /**
   * Format as a 2digit inte.g. 2 become "02"
   */
  def s02(n: Int): String = f"$n%02d"

  def empty(setPoint: Int): Schedule = {
    val scheduleKey: Key = Key(KeyKind.scheduleKey, setPoint)
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


  def apply(setPoint: Int): Schedule = new Schedule(Key(KeyKind.scheduleKey, setPoint))


  implicit val fmtSchedule: Format[Schedule] = Json.format[Schedule]


  override def parse(jsValue: JsValue): FieldValue = jsValue.as[Schedule]

  override val name: String = "Schedule"
  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.scheduleKey

}





