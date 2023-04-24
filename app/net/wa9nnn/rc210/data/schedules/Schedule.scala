package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.key.KeyFactory.ScheduleKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.util.MacroSelect
import play.api.libs.json.{Format, JsValue, Json}

/**
 *
 * @param key                  for [[ScheduleKey]]
 * @param dayOfWeek            enumerated
 * @param weekInMonth          e.g 1 == 1st week in month.
 * @param monthOfYear          enumerated
 * @param time                 when this runs on selected day.
 * @param selectedMacroToRun   e.g. "macro42"
 */
case class Schedule(override val key: ScheduleKey,
                    dayOfWeek: DayOfWeek,
                    weekInMonth: WeekInMonth,
                    monthOfYear: MonthOfYear,
                    time: FieldTime,
                    selectedMacroToRun: MacroSelect,
                    enabled: FieldBoolean) extends ComplexFieldValue[ScheduleKey] with TriggerNode with RenderMetadata {

  val description: String = {
    val week = s" Week: $weekInMonth"
    s"$monthOfYear$week on $dayOfWeek at $time"
  }

  override def toRow: Row = {
    implicit val k: ScheduleKey = key
    val name: Cell = k.namedCell()
    val dow: Cell = dayOfWeek.toCell(RenderMetadata(DayOfWeek.name))
    val woy: Cell = Cell.rawHtml(monthOfYear.toHtmlField(RenderMetadata(MonthOfYear.name)))
    val localTime: Cell = time.toCell(RenderMetadata("Time"))
    val macroToRun: Cell = selectedMacroToRun.toCell(RenderMetadata(MacroSelect.name))

    Row(Seq(
      name,
      enabled.toCell(RenderMetadata("Enabled")),
      dow,
      weekInMonth.toCell(RenderMetadata(WeekInMonth.name)),
      woy,
      localTime,
      macroToRun
    ))

  }
  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = Seq("//todo")


  override def display: String = description

  override def param: String = FieldKey("Schedule", key).param

  override def prompt: String = "Runs a Macro on a "

  override def units: String = ""


  override val fieldName: String = "Schedule"

  override def toJsonValue: JsValue = Json.toJson(this)

  override def canRunMacro(macroKey: KeyFactory.MacroKey): Boolean = selectedMacroToRun.value == macroKey
}

object Schedule extends LazyLogging with ComplexExtractor {

  def empty(setPoint: Int): Schedule = {
    val scheduleKey: ScheduleKey = KeyFactory(KeyKind.scheduleKey, setPoint)
    new Schedule(
      key = scheduleKey,
      dayOfWeek = new DayOfWeek(),
      weekInMonth = new WeekInMonth(),
      monthOfYear = new MonthOfYear(),
      time = new FieldTime(),
      selectedMacroToRun = new MacroSelect(),
      enabled = FieldBoolean()
    )
  }

  def header(count: Int): Header = Header(s"Schedules ($count)", "SetPoint", "Macro", "DOW", "WeekInMonth", "MonthOfYear", "LocalTime")

  override def extract(memory: Memory): Seq[FieldEntry] = {

    val scheduleBuilder = new ScheduleBuilder(memory.iterator8At(616))
    // Collection values from various places in Memory.
    scheduleBuilder.putDow()
    scheduleBuilder.putMoy()
    scheduleBuilder.putHours()
    scheduleBuilder.putMinutes()
    scheduleBuilder.putMacro()


    scheduleBuilder.slots.toIndexedSeq.map { schedule =>
      FieldEntry(this, FieldKey("Schedule", schedule.key), schedule)
    }
  }

  def apply(key: ScheduleKey)(implicit values: Map[String, String]): Schedule = {
    Schedule(key = key,
      dayOfWeek = DayOfWeek(),
      weekInMonth = WeekInMonth(),
      monthOfYear = MonthOfYear(),
      time = FieldTime(),
      selectedMacroToRun = MacroSelect(),
      enabled = FieldBoolean("Enabled")
    )
  }

  import net.wa9nnn.rc210.key.KeyFormats._
  implicit val fmtSchedule: Format[Schedule] = Json.format[Schedule]


  override def parse(jsValue: JsValue): FieldValue = jsValue.as[Schedule]

  override val name: String = "Schedule"
  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.scheduleKey
}




