package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.JsonFormatUtils._
import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.data.schedules.Schedule.{kind, s02}
import net.wa9nnn.rc210.key.KeyFactory.{MacroKey, ScheduleKey}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.Memory
import play.api.libs.json.{Format, JsValue, Json}

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
    //    implicit val k: ScheduleKey = key
    //    val name: Cell = k.namedCell()
    //    val dow: Cell = dayOfWeek.toCell(RenderMetadata(DayOfWeek.name))
    //    val woy: Cell = Cell.rawHtml(monthOfYear.toHtmlField(RenderMetadata(MonthOfYear.name)))
    //    val localTime: Cell = time.toCell(RenderMetadata("Time"))
    //    val macroToRun: Cell = selectedMacroToRun.toCell(RenderMetadata(MacroSelect.name))
    //
    //    Row(Seq(
    //      name,
    //      enabled.toCell(RenderMetadata("Enabled")),
    //      dow,
    //      weekInMonth.toCell(RenderMetadata(WeekInMonth.name)),
    //      woy,
    //      localTime,
    //      macroToRun
    //    ))
    throw new NotImplementedError() //todo
  }

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {

//    224 30/04/2023 15:55:47 IRP_MJ_WRITE DOWN  31 2a 34 30 30 31 33 2a 31 30 2a 30 30 2a 30 37 2a 30 30 2a 30 33 0d  1*40013*10*00*07*00*03. 23 23 COM3

    // 1*40013*10*00*07*00*03
    // 1*40011*00*00*25*00*2
    //      1*40011*00*00*25*00*2
//         1*40011*00*00*25*00*2
      // 1*40011*0*00*25*00*01  via rCP
    //1*400130*0*00*25*00*90
    //1*4001 S * DOW * MOY * Hours * Minutes * Macro
    val setPoint: String = key.number.toString
    val sDow: String = dow.dowNumber().toString
    val moy: String = s02(monthOfYear.ordinal())
    val hours: String = s02(hour)
    val minutes: String = s02(minute)
    val sMacro =s02(macroKey.number)

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



