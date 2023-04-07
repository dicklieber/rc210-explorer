package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Header, Row}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.data.named.NamedSource
import net.wa9nnn.rc210.key.KeyFactory.ScheduleKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.MemoryBuffer
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


//  override def toJsValue: JsValue = {
//    val fields: JsObject = Json.obj(
//      "key" -> key.toString,
//      "dayOfWeek" -> dayOfWeek.display,
//      "weekInMonth" -> weekInMonth.display,
//      "monthOfYear" -> monthOfYear.display,
//      "time" -> time.display,
//      "selectedMacroToRun" -> selectedMacroToRun.display,
//      "enabled" -> enabled.display
//    )
//    fields
//  }

  override def toRow: Row = {
    Row(key.toCell, selectedMacroToRun.toCell(this), dayOfWeek, weekInMonth, monthOfYear, time)
  }


  val description: String = {
    val week = s" Week: $weekInMonth"
    s"$monthOfYear$week on $dayOfWeek at $time"
  }

  override def triggerRow: Row = {
    Row(key.toCell, description)
  }

  def toRow()(implicit namedSource: NamedSource): Row = {
    implicit val k: ScheduleKey = key
    val keyName = namedSource.get(key).getOrElse("")
    val name: Cell = Cell.rawHtml(views.html.fieldNamedKey(key, keyName, RenderMetadata("name")).toString())
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

  override val triggerEnabled: Boolean = nodeEnabled

  override def triggerDescription: String = toString


  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???


  override def display: String = description

  override def param: String = FieldKey("Schedule", key).param

  override def prompt: String = "Runs a Macro on a "

  override def units: String = ""

  override def macroToRun: KeyFactory.MacroKey = selectedMacroToRun.value

  override val fieldName: String = "Schedule"

  override def toJsonValue: JsValue = Json.toJson(this)

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

  override def extract(memory: MemoryBuffer): Seq[FieldEntry] = {

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

  override val fieldName: String = "Schedule"
  override val kind: KeyKind = KeyKind.scheduleKey
  import net.wa9nnn.rc210.key.KeyFormats._
  implicit val fmtSchedule: Format[Schedule] = Json.format[Schedule]


  override def jsonToField(jsValue: JsValue): FieldValue = jsValue.as[Schedule]
}




