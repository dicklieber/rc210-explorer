package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.*
import controllers.routes
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.data.field.schedule.{DayOfWeek, Week}
import net.wa9nnn.rc210.data.schedules.ScheduleNode.s02
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.nav.CheckBoxCell
import net.wa9nnn.rc210.ui.{Display, EditFlowButtons, TableSectionButtons}
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import views.html.{fieldIndex, scheduleEdit, schedules}

/**
 *
 * @param key                     for [[ScheduleKey]]
 * @param dow                     [[DayOfWeek]] or [[WeekAndDow]].
 * @param monthOfYear             enumerated
 * @param hour                    when this runs on selected day.
 * @param minute                  when this runs on selected day.
 * @param macroKeys               e.g. "macro42"
 * @param enabled                 duh
 */
case class ScheduleNode(override val key: Key,
                        dow: DayOfWeek = DayOfWeek.EveryDay,
                        week: Week = Week.Every,
                        monthOfYear: MonthOfYearSchedule = MonthOfYearSchedule.Every,
                        hour: Int = 0,
                        minute: Int = 0,
                        macroKey: Key = Key(KeyKind.Macro, 1),
                        override val enabled: Boolean = false) extends ComplexFieldValue(macroKey):

  val time: String = f"$hour%02d:$minute%02d"

  override def toRow: Row = {
    val enableCell: Cell = CheckBoxCell(enabled)
    Row(
      EditFlowButtons.cell(fieldKey),
      key.keyWithName,
      enableCell,
      dow,
      week,
      monthOfYear,
      f"$hour%02d:$minute%02d",
      macroKey.keyWithName
    )
  }

  private val rows: Seq[Row] = Seq(
    "Key" -> key.keyWithName,
    "Day Of Week" -> dow,
    "Week" -> week,
    "Month" -> monthOfYear,
    "Hour" -> hour,
    "Minute" -> minute,
    "Macro" -> macroKey.keyWithName
  ).map(Row(_))

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

  override def tableSection(fieldKey: FieldKey): TableSection =
    TableSectionButtons(fieldKey, rows: _*)

  override def displayCell: Cell =
    KvTable.inACell(
      rows:_*
    )

  override def toJsValue: JsValue = Json.toJson(this)

object ScheduleNode extends LazyLogging with ComplexExtractor[ScheduleNode]:
  override val keyKind: KeyKind = KeyKind.Schedule

  def unapply(schedule: ScheduleNode): Option[(Key, DayOfWeek, Week, MonthOfYearSchedule, Int, Int, Key, Boolean)] =
    Some(schedule.key, schedule.dow, schedule.week, schedule.monthOfYear, schedule.hour, schedule.minute, schedule.macroKey, schedule.enabled)

  override val form: Form[ScheduleNode] = Form[ScheduleNode](
    mapping(
      "key" -> of[Key],
      "dow" -> DayOfWeek.formField,
      "week" -> Week.formField,
      "monthOfYear" -> MonthOfYearSchedule.formField,
      "hour" -> number(0, 23),
      "minute" -> number(0, 59),
      "macroKey" -> of[Key],
      "enabled" -> boolean
    )(ScheduleNode.apply)(ScheduleNode.unapply)
  )

  /**
   * Format as a 2digit inte.g. 2 become "02"
   */
  def s02(n: Int): String = f"$n%02d"

  def empty(setPoint: Int): ScheduleNode = {
    val scheduleKey: Key = Key(KeyKind.Schedule, setPoint)
    new ScheduleNode(
      key = scheduleKey
    )
  }

  def header(count: Int): Header = Header(s"Schedules ($count)",
    "",
    "Schedule",
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
      FieldEntry(this, FieldKey(schedule.key), schedule)
    }
  }

  def apply(setPoint: Int): ScheduleNode = new ScheduleNode(Key(KeyKind.Schedule, setPoint))

  implicit val fmtSchedule: Format[ScheduleNode] = Json.format[ScheduleNode]

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[ScheduleNode]

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    schedules(fieldEntries.map(_.value[ScheduleNode]))

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html = {
    val value = form.fill(fieldEntry.value)
    scheduleEdit(value, fieldEntry.fieldKey)
  }

  override def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate] = {
    bind(form.bindFromRequest(data).get)
  }





