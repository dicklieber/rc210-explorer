package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.field.MonthOfYearSchedule
import net.wa9nnn.rc210.data.field.schedule.{DayOfWeek, Week}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.util.Chunk

object ScheduleBuilder extends LazyLogging {
  // indices into a [[Chunk]]
  private val DOW = 0
  private val MOY = 1
  private val HOUR = 2
  private val MINUTE = 3
  private val MACR0 = 4
  val fieldsInRC210Schedule = 5

  def apply(memory: Memory): Seq[ScheduleNode] = {
    // Each chunk is one RC-210 memory setpoint rc2input. With all the setpopint's value withinb that chunk.
    implicit val chunks: Iterator[Chunk] = memory.chunks(616, KeyKind.Schedule.maxN, fieldsInRC210Schedule).iterator

    val chunk = chunks.next()
    val dows: Seq[DayOfWeek] = chunk.map(DayOfWeek find _).toSeq
    val moys: Seq[MonthOfYearSchedule] = chunks.next().map(MonthOfYearSchedule find _).toSeq
    val hours: Seq[Int] = chunks.next().toSeq
    val minutes: Seq[Int] = chunks.next().toSeq
    val macros: Seq[Key] = chunks.next().map(i => Key(KeyKind.Macro, i)).toSeq

    for
      n <- 0 until KeyKind.Schedule.maxN
    yield
      val scheduleKey = Key(KeyKind.Schedule, n + 1)
      ScheduleNode(key = scheduleKey,
        dow = dows(n),
        monthOfYear = moys(n),
        hour = hours(n),
        minute = minutes(n),
        macroKey = macros(n)
      )


/*
    for {
      n <- 0 until KeyKind.Schedule.maxN
    } yield {
      val scheduleKey = Key(KeyKind.Schedule, n + 1)

  /*    val sDow: String = chunks(DOW)(n).toString
      val (week: Week, dow: DayOfWeek) = {
        val ints: Array[Int] = sDow.toCharArray.map(_.asDigit)
        try {
          ints match {
            case Array(dow: Int) =>
              Week.Every -> DayOfWeek.find(dow)
            case Array(wInMo, dow) =>
              Week.find(wInMo) -> DayOfWeek.find(dow)
            case _ =>
              logger.error(s"DOW must be 1 or 2 chars, got $sDow")
              Week.Every -> DayOfWeek.EveryDay
          }
        } catch {
          case e: Exception =>
            logger.error(s"Extracting DOW for: $scheduleKey data: ${ints.mkString(",")}", e)
            Week.Every -> DayOfWeek.EveryDay
        }
      }
  */   
      val dow:DayOfWeek = DayOfWeek.find(chunks(MOY)(n))
      val monthOfYear: MonthOfYearSchedule = MonthOfYearSchedule.find(chunks(MOY)(n))
      val hour = chunks(HOUR)(n)
      val minute = chunks(MINUTE)(n)
      val macr0 = Key(KeyKind.Macro, (chunks(MACR0)(n) + 1))
      ScheduleNode(key = scheduleKey,
        dow = dow,
        monthOfYear = monthOfYear,
        hour = hour,
        minute = minute,
        macroKey = macr0,
      )
    }
*/
  }
}