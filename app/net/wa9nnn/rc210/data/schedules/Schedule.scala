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
                    dow: DowBase = DayOfWeek.EveryDay,
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
    val dowParam = FieldKey("dow", k).param
    val moyParam = FieldKey("moy", k).param
    val weekParam = FieldKey("week", k).param

    val (dowCell: Cell, moyCell: Cell, weekCell: Cell) = {
      dow match {
        case dayOfWeek: DayOfWeek =>
          (dowSelect.toCell(dowParam, dayOfWeek), moySelect.toCell(moyParam), weekSelect.toCell(weekParam))
        case WeekAndDow(week, dayOfWeek) =>
          (dowSelect.toCell(dowParam, dayOfWeek), moySelect.toCell(moyParam), weekSelect.toCell(weekParam, week))
      }
    }
    val name: Cell = k.namedCell()
    val localTime: Cell = {
      val h = if (hour > 24) // RC-210 use > 24 as disabled.
        0 else hour

      Cell(LocalTime.of(h, minute).toString).withName(FieldKey("time", k).param)
    }
    val macroToRun: Cell = MacroSelect(macroKey).toCell(RMD(name = FieldKey("macro", k).param))

    Row(
      name,
      Cell(enabled).withName(FieldKey("enabled", k).param),
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
    val moy: String = s02(monthOfYear.ordinal())
    val hours: String = s02(hour)
    val minutes: String = s02(minute)
    val sMacro = s02(macroKey.number)

    val command = s"1*4001$setPoint*$sDow*${moy}*$hours*$minutes*$sMacro"
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

  def header(count: Int): Header = Header(s"Schedules ($count)", "SetPoint", "Macro", "DOW", "WeekInMonth", "MonthOfYear", "LocalTime")

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

  val weekSelect = new EnumSelect[Week](Week.values())
  val dowSelect = new EnumSelect[DayOfWeek](DayOfWeek.values())
  val moySelect = new EnumSelect[MonthOfYear](MonthOfYear.values())

  def apply(setPoint: Int): Schedule = new Schedule(KeyFactory.scheduleKey(setPoint))

  import net.wa9nnn.rc210.key.KeyFormats._

  implicit val fmtWeek: Format[Week] = javaEnumFormat[Week]
  implicit val fmtDayOfWeek: Format[DayOfWeek] = javaEnumFormat[DayOfWeek]
  implicit val fmtMonthOfYear: Format[MonthOfYear] = javaEnumFormat[MonthOfYear]
  implicit val fmtWeekAndDow: Format[WeekAndDow] = Json.format[WeekAndDow]
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

case class WeekAndDow(week: Week, dayOfWeek: DayOfWeek) extends DowBase {
  override def getFlavor: DowFlavor = DowFlavor.weekAndDow

  override def dowNumber(): Int = {
    week.ordinal() + dayOfWeek.ordinal() * 10
  }
}



