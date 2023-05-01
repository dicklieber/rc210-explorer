package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.JsonFormatUtils._
import com.wa9nnn.util.tableui.{Cell, Header, Row}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.data.schedules.Schedule.{dowSelect, moySelect, s02, weekSelect}
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, ScheduleKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.EnumSelect
import net.wa9nnn.rc210.util.MacroSelect
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
case class Schedule(override val key: ScheduleKey,
                    dow: DayOfWeek = DayOfWeek.EveryDay,
                    week: Week = Week.Every,
                    monthOfYear: MonthOfYear = MonthOfYear.Every,
                    hour: Int = 0,
                    minute: Int = 0,
                    macroKey: MacroKey = KeyFactory.defaultMacroKey,
                    enabled: Boolean = false) extends ComplexFieldValue[ScheduleKey] with TriggerNode with RenderMetadata {

  val description: String = {
    //    val week = s" Week: $weekInMonth"
    //    s"$monthOfYear$week on $dayOfWeek at $time"
    "" //todo"
  }

  override def toRow: Row = {
    implicit val k: ScheduleKey = key
    val name: Cell = k.namedCell()
    val enableCell: Cell = FieldBoolean.toCell(enabled, "enabled")
    val dowCell: Cell = dowSelect.toCell(dow)
    val weekCell = weekSelect.toCell(week)
    val moyCell = moySelect.toCell(monthOfYear)

    val localTime: Cell = {
      val h = if (hour > 24) // RC-210 use > 24 as disabled.
        0 else hour

      val localTime1 = LocalTime.of(h, minute)
      val html = views.html.fieldTime(localTime1, RMD(name = "time")).toString()
      Cell.rawHtml(html)
    }
    val macroToRun: Cell = {
      MacroSelect(macroKey).toCell(RMD(name = "macro"))
    }

    Row(
      name,
      enableCell,
      dowCell,
      moyCell,
      weekCell,
      localTime,
      macroToRun
    )
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val setPoint: String = key.number.toString
    val sDow: String = dow.dowNumber().toString
    val sWeek: String = week match {
      case Week.Every =>
        ""
      case d: Week =>
        d.ordinal().toString
    }
    val moy: String = s02(monthOfYear.ordinal())
    val hours: String = s02(hour)
    val minutes: String = s02(minute)
    val sMacro = s02(macroKey.number)

    val command = s"1*4001$setPoint*$sWeek$sDow*${moy}*$hours*$minutes*$sMacro"
    Seq(command)
  }


  override def display: String = description

  override def param: String = FieldKey("Schedule", key).param

  override def prompt: String = "Runs a Macro on a "

  override def units: String = ""


  override val fieldName: String = "Schedule"

  override def toJsonValue: JsValue = Json.toJson(this)

  override def canRunMacro(candidate: KeyFactory.MacroKey): Boolean = macroKey == candidate
}

object Schedule extends LazyLogging with ComplexExtractor[ScheduleKey] {

  def s02(n: Int): String = f"$n%02d"

  def empty(setPoint: Int): Schedule = {
    val scheduleKey: ScheduleKey = KeyFactory(KeyKind.scheduleKey, setPoint)
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

  def fromForm(key: ScheduleKey, kv: Map[String, String]): Schedule = {
    val enabled: Boolean = kv("enabled") == "true"
    val week: Week = weekSelect.fromForm(kv("week"))
    val dow: DayOfWeek = dowSelect.fromForm(kv("dow"))
    val moy: MonthOfYear = moySelect.fromForm(kv("moy"))
    val macroKey:MacroKey = {
      val sMacroKey: String = kv("macro")
      KeyFactory(sMacroKey)
    }
    val localTime = LocalTime.parse(kv("time"))
    val hour = localTime.getHour
    val minute = localTime.getMinute
    new Schedule(
      key = key,
      dow = dow,
      week = week,
      monthOfYear = moy,
      hour = hour,
      minute = minute,
      enabled = enabled,
      macroKey = macroKey
    )
  }


  val dowSelect = new EnumSelect[DayOfWeek]("dow", DayOfWeek.values())
  val weekSelect = new EnumSelect[Week]("week", Week.values())
  val moySelect = new EnumSelect[MonthOfYear]("moy", MonthOfYear.values())

  def apply(setPoint: Int): Schedule = new Schedule(KeyFactory.scheduleKey(setPoint))

  import net.wa9nnn.rc210.key.KeyFormats._

  implicit val fmtWeek: Format[Week] = javaEnumFormat[Week]
  implicit val fmtDayOfWeek: Format[DayOfWeek] = javaEnumFormat[DayOfWeek]
  implicit val fmtMonthOfYear: Format[MonthOfYear] = javaEnumFormat[MonthOfYear]
  implicit val fmtDaoBase: Format[DowBase] = new Format[DowBase] {
    override def writes(o: DowBase) = {
      throw new NotImplementedError() //todo
    }

    override def reads(json: JsValue) = ???
  }


  implicit val fmtSchedule: Format[Schedule] = Json.format[Schedule]


  override def parse(jsValue: JsValue): FieldValue = jsValue.as[Schedule]

  override val name: String = "Schedule"
  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.scheduleKey
}





