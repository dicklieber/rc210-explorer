package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.data.field.schedule.DayOfWeek
import net.wa9nnn.rc210.data.schedules.ScheduleNode.s02
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.nav.BooleanCell
import net.wa9nnn.rc210.ui.{ButtonCell, TableSectionButtons}
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import views.html.{fieldIndex, scheduleEdit}

/**
 *
 * @param key                     for [[ScheduleKey]]
 * @param dow                     [[DayOfWeek]] or [[WeekAndDow]].
 * @param monthOfYear             enumerated
 * @param hour                    when this runs on selected day.
 * @param minute                  when this runs on selected day.
 * @param macroKeys               e.g. "macro42"
 */
case class ScheduleNode(override val key: Key,
                        dow: DayOfWeek = DayOfWeek.EveryDay,
                        monthOfYear: MonthOfYearSchedule = MonthOfYearSchedule.Every,
                        hour: Int = 0,
                        minute: Int = 0,
                        macroKey: Key = Key(KeyKind.Macro, 1)) extends ComplexFieldValue(macroKey):

  val time: String = f"$hour%02d:$minute%02d"

  override def toRow: Row = {
    Row(
      ButtonCell.edit(fieldKey),
      key.keyWithName,
      BooleanCell(enabled),
      dow,
      monthOfYear,
      f"$hour%02d:$minute%02d",
      macroKey.keyWithName
    )
  }

  private def rows(): Seq[Row] = Seq(
    "Key" -> key.keyWithName,
    "Day Of Week" -> dow,
    "Month" -> monthOfYear,
    "Hour" -> hour,
    "Minute" -> minute,
    "Macro" -> macroKey.keyWithName
  ).map(Row(_))
  
  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val setPoint: String = key.rc210Value.toString
    val sDow: String = dow.rc210Value.toString
    val moy: String = s02(monthOfYear.rc210Value)
    val hours: String = s02(hour)
    val minutes: String = s02(minute)
    val sMacro = s02(macroKey.rc210Value)

    val command = s"1*4001$setPoint$sDow*${moy}*$hours*$minutes*$sMacro"
    Seq(command)
  }

  override def tableSection(fieldKey: FieldKey): TableSection =
    TableSectionButtons(fieldKey, rows(): _*)

  override def displayCell: Cell =
    KvTable.inACell(
      rows():_*
    )

  override def toJsValue: JsValue = Json.toJson(this)

object ScheduleNode extends LazyLogging with ComplexExtractor[ScheduleNode]:
  override val keyKind: KeyKind = KeyKind.Schedule

  def unapply(schedule: ScheduleNode): Option[(Key, DayOfWeek, MonthOfYearSchedule, Int, Int, Key)] =
    Some(schedule.key, schedule.dow, schedule.monthOfYear, schedule.hour, schedule.minute, schedule.macroKey)

  override val form: Form[ScheduleNode] = Form[ScheduleNode](
    mapping(
      "key" -> of[Key],
      "dow" -> DayOfWeek.formField,
      "monthOfYear" -> MonthOfYearSchedule.formField,
      "hour" -> number(0, 23),
      "minute" -> number(0, 59),
      "macroKey" -> of[Key],
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

  override def extract(memory: Memory): Seq[FieldEntry] = 
    ScheduleBuilder(memory).map { schedule =>
      FieldEntry(this, schedule)
    }

  def apply(setPoint: Int): ScheduleNode = new ScheduleNode(Key(KeyKind.Schedule, setPoint))

  implicit val fmtSchedule: Format[ScheduleNode] = Json.format[ScheduleNode]

  override def parse(jsValue: JsValue): FieldValue = jsValue.as[ScheduleNode]

//  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
//    schedules(fieldEntries.map(_.value[ScheduleNode]))
//
//    fieldEntries.map(_.value.toRow)

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val table = Table(Header(s"Schedules  (${fieldEntries.length})",
      "",
      "Schedule",
      "Day in Week",
      "Month",
      Cell("Week").withToolTip("Week in month"),
      "Time",
      "Macro to Run"
    ),
      fieldEntries.map(_.value.toRow)
    )
    fieldIndex(keyKind, table)





  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html = {
    val value = form.fill(fieldEntry.value)
    scheduleEdit(value, fieldEntry.fieldKey)
  }

  override def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate] = {
    bind(form.bindFromRequest(data).get)
  }





