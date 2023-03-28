package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.MemoryExtractor
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field._
import net.wa9nnn.rc210.key.KeyFactory.ScheduleKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import net.wa9nnn.rc210.util.MacroSelect
import play.api.libs.json.{JsString, JsValue}

import java.time.LocalTime

/**
 *
 * @param key          for [[ScheduleKey]]
 * @param dayOfWeek    enumerated
 * @param weekInMonth  e.g 1 == 1st week in month.
 * @param monthOfYear  enumerated
 * @param localTime    illegal times are None.
 * @param selectedMacroToRun   e.g. "macro42"
 */
case class Schedule(override val key: ScheduleKey,
                    dayOfWeek: DayOfWeek,
                    weekInMonth: Option[Int],
                    monthOfYear: MonthOfYear,
                    localTime: Option[LocalTime],
                    selectedMacroToRun: MacroSelect)
  extends FieldWithFieldKey[ScheduleKey] with TriggerNode with RenderMetadata {


  override def toRow: Row = {
    Row(key.toCell, selectedMacroToRun.toCell(this), dayOfWeek, weekInMonth, monthOfYear, localTime)
  }

  override val nodeEnabled: Boolean = localTime.nonEmpty

  val description: String = {
    localTime.map { localTime =>
      val week = s" Week: $weekInMonth"
      s"$monthOfYear$week on $dayOfWeek at $localTime"
    }
      .getOrElse("-disabled-")
  }

  override def triggerRow: Row = {
    Row(key.toCell, description)
  }


  override val triggerEnabled: Boolean = nodeEnabled

  override def triggerDescription: String = toString

  override def toJsValue: JsValue = JsString(description) //todo JsObject of Schedule.

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommand(fieldEntry: FieldEntry): String = ???

  /**
   * Render as HTML. Either a single field of an entire HTML Form.
   *
   */
  override def toHtmlField(renderMetadata: RenderMetadata): String =
    throw new IllegalStateException("Cannot render schedule as a single field!")

  override def display: String = description

  override def param: String = FieldKey("Schedule", key).param

  override def prompt: String = "Runs a Macro on a schedule."

  override def unit: String = ""

  override def macroToRun: KeyFactory.MacroKey = selectedMacroToRun.value

  override val fieldName: String = "Schedule"
}

object Schedule extends LazyLogging with MemoryExtractor {

  def empty(setPoint: Int): Schedule = {
    val scheduleKey: ScheduleKey = KeyFactory(KeyKind.scheduleKey, setPoint)
    new Schedule(
      key = scheduleKey,
      dayOfWeek = DayOfWeek(),
      weekInMonth = None,
      monthOfYear = MonthOfYear(scheduleKey),
      localTime = None,
      selectedMacroToRun = MacroSelect()
    )
  }

  def header(count: Int): Header = Header(s"Schedules ($count)", "SetPoint", "Macro", "DOW", "WeekInMonth", "MonthOfYear", "LocalTime")

  override def extract(memory: Memory): Seq[FieldEntry] = {

    def collect(php: String): Seq[Int] = {
      memory(SlicePos(php)).data
    }

    val scheduleBuilder = new ScheduleBuilder()
    // Collection values from various places in Memory.
    scheduleBuilder.putDow(collect("//SetPointDOW - 616-655"))
    scheduleBuilder.putMoy(collect("//SetPointMOY - 656-695"))
    scheduleBuilder.putHours(collect("//SetPointHours - 696-735"))
    scheduleBuilder.putMinutes(collect("//SetPointMinutes - 736-775"))
    scheduleBuilder.putMacro(collect("//SetPointMacro - 776-815"))


    scheduleBuilder.slots.toIndexedSeq.map { schedule =>
      FieldEntry(this, FieldKey("Schedule", schedule.key), schedule)
    }
  }

  override val fieldName: String = "Schedule"
  override val kind: KeyKind = KeyKind.scheduleKey
}




