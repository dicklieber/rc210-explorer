package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.data.field.{FieldDef, FieldEntry, MonthOfYearSchedule}
import net.wa9nnn.rc210.data.schedules.WeekInMonth.Every
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.util.Chunk

object ScheduleBuilder extends LazyLogging {
  // indices into a [[Chunk]]
  private val DOW = 0 // includes WeekInMonth
  private val MOY = 1
  private val HOUR = 2
  private val MINUTE = 3
  private val MACR0 = 4
  val fieldsInRC210Schedule = 5

  def apply(memory: Memory, fieldDef: FieldDef[?]): Seq[FieldEntry] = {
    val keyMetadata = fieldDef.keyMetadata
    // Each chunk is one RC-210 memory setpoint rc2input. With all the setpopint's value withinb that chunk.
    implicit val chunks: Iterator[Chunk] = memory.chunks(616, keyMetadata.maxN, fieldsInRC210Schedule).iterator

    val chunk = chunks.next()
    val dows: Seq[DayOfWeekField] = chunk.map(DayOfWeekField(_)).toSeq
    val moys: Seq[MonthOfYearSchedule] = chunks.next().map(MonthOfYearSchedule find _).toSeq
    val hours: Seq[Hour] = chunks.next().map(Hour find _).toSeq
    val minutes: Seq[Int] = chunks.next().toSeq
    val macros: Seq[Key] = chunks.next().map(i => Key(KeyMetadata.Macro, i)).toSeq

    for
      n <- 0 until keyMetadata.maxN
    yield
      val key = Key(keyMetadata, n + 1)
      val dayOfWeekField = dows(n)
      val scheduleNode = ScheduleNode(
        dayOfWeek = dayOfWeekField.dayOfWeek,
        weekInMonth = dayOfWeekField.weekInMonth,
        monthOfYear = moys(n),
        hour = hours(n),
        minute = minutes(n),
        macroKey = macros(n))
      FieldEntry(fieldDef, key, scheduleNode)
  }
}