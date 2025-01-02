package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.data.schedules.ScheduleNode.s02
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.{ButtonCell, FormData, TableSectionButtons}
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import views.html.{fieldIndex, scheduleEdit}

/**
 *
 * @param key                           for [[ScheduleKey]]
 * @param dayOfWeek                     [[DayOfWeekField]] or [[WeekAndDow]].
 * @param monthOfYear                   enumerated
 * @param hour                          when this runs on selected day.
 * @param minute                        when this runs on selected day.
 * @param macroKey                      e.g. "macro42"
 */
case class ScheduleNode(dayOfWeek: DayOfWeek = DayOfWeek.EveryDay,
                        weekInMonth: WeekInMonth = WeekInMonth.Every,
                        monthOfYear: MonthOfYearSchedule = MonthOfYearSchedule.Every,
                        hour: Hour = Hour.Every,
                        minute: Int = 0,
                        macroKey: Key = Key(KeyMetadata.Macro, 1)) extends FieldValueComplex(macroKey):

  override def toRow(key: Key): Row = {
    Row(
      ButtonCell.edit(key),
      dayOfWeek,
      weekInMonth,
      monthOfYear,
      hour.entryName,
      minute,
      macroKey.keyWithName
    )
  }

  private val rows: Seq[Row] = Seq(
    "Day Of Week" -> dayOfWeek,
    "Week In Month" -> weekInMonth,
    "Month" -> monthOfYear,
    "Hour" -> hour.entryName,
    "Minute" -> minute,
    "Macro" -> macroKey.keyWithName
  ).map(Row(_))

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(key: Key): Seq[String] =
    val setPoint: String = key.rc210Number.toString
    val sDow: String = dayOfWeek.rc210Value.toString
    val moy: String = s02(monthOfYear.rc210Value)
    val hours: String = s02(hour.rc210Value)
    val minutes: String = s02(minute)
    val sMacro = s02(key.rc210Number.get)

    val dowDigits = WeekInMonth.translate(weekInMonth, dayOfWeek)
    //todo
    val command = s"1*4001$setPoint*$dowDigits*$moy*$hours*$minutes*$sMacro"
    Seq(command)

    //1 * 4 0 0 1 3 9 * 5 6 * 0 0 * 9 9 * 0 0 * 9 0

  override def tableSection(key: Key): TableSection =
    TableSectionButtons(key, rows: _*)

  override def displayCell: Cell =
    KvTable.inACell(
      rows: _*
    )

object ScheduleNode extends LazyLogging with FieldDefComplex[ScheduleNode]:
  override val keyMetadata: KeyMetadata = KeyMetadata.Schedule

  def unapply(schedule: ScheduleNode): Option[( DayOfWeek, WeekInMonth, MonthOfYearSchedule, Hour, Int, Key)] =
    Some( schedule.dayOfWeek, schedule.weekInMonth, schedule.monthOfYear, schedule.hour, schedule.minute, schedule.macroKey)

  override val form: Form[ScheduleNode] = Form[ScheduleNode](
    mapping(
      "dayOfWeek" -> DayOfWeek.formField,
      "weekInMonth" -> WeekInMonth.formField,
      "monthOfYear" -> MonthOfYearSchedule.formField,
      "hour" -> Hour.formField,
      "minute" -> number(0, 59),
      "macroKey" -> of[Key],
    )((dayOfWeek: DayOfWeek, weekInMonth: WeekInMonth, monthOfYear: MonthOfYearSchedule, hour: Hour, minute: Int, macroKey: Key) => ScheduleNode.apply(dayOfWeek, weekInMonth, monthOfYear, hour, minute, macroKey))(ScheduleNode.unapply)
  )

  /**
   * Format as a 2digit e.g. 2 become "02"
   */
  def s02(n: Int): String = f"$n%02d"

  def empty(setPoint: Int): ScheduleNode = {
    val scheduleKey: Key = Key(KeyMetadata.Schedule, setPoint)
    new ScheduleNode()
  }

  def header(count: Int): Header = Header(s"Schedules ($count)",
    "",
    "Schedule",
    "Day in Week",
    "Month",
    Cell("Week").withToolTip("Week in month"),
    "Time",
    "Macro To Run")


  override def extract(memory: Memory): Seq[FieldEntry] =
    ScheduleBuilder(memory, this)

  def apply(setPoint: Int): ScheduleNode = new ScheduleNode()

  implicit val fmt: Format[ScheduleNode] = Json.format[ScheduleNode]
  
  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val table = Table(header(fieldEntries.length),
      fieldEntries.map(fe => {
        val value:ScheduleNode = fe.value
        value.toRow(fe.key)
      })
    )
    fieldIndex(keyMetadata, table)

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val value = form.fill(fieldEntry.value)
    scheduleEdit(value, fieldEntry.key)

  override def bind(formData: FormData): Iterable[UpdateCandidate] =
    for
      key <- formData.maybeKey
    yield
      UpdateCandidate(key, form.bind(formData.bindable).get)
  
  

