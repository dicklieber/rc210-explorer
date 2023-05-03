package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.field.{DayOfWeek, MonthOfYearSchedule, Week}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.util.Chunk

object ScheduleBuilder extends LazyLogging {
  // indices into a [[Chunk]]
  private val DOW = 0
  private val MOY = 1
  private val HOUR = 2
  private val MINUTE = 3
  private val MACR0 = 4
  private val ENABLE = 5
  val fieldsInRC210Schedule = 6

  def apply(memory: Memory): Seq[Schedule] = {
    // Each chunk is one RC-210 memory setpoint field. With all the setpopint's value withinb that chunk.
    implicit val chunks: Seq[Chunk] = memory.chunks(616, KeyKind.scheduleKey.maxN(), fieldsInRC210Schedule)


    for {
      n <- 0 until KeyKind.scheduleKey.maxN()
    } yield {
      val scheduleKey = KeyFactory.scheduleKey(n + 1)

      val sDow = chunks(DOW)(n).toString
      val (week: Week, dow: DayOfWeek) = {
        try {
          sDow.toCharArray.map(_.asDigit) match {
            case Array(dow) =>
              Week.Every -> DayOfWeek.values()(dow)
            case Array(wInMo, dow) =>
              Week.values()(wInMo) -> DayOfWeek.values()(dow)
            case _ =>
              logger.error(s"DOW must be 1 or 2 chars, got $sDow")
              Week.Every -> DayOfWeek.EveryDay
          }
        } catch {
          case e: Exception =>
            logger.error(s"Extracting DOW for: $scheduleKey", e)
            Week.Every -> DayOfWeek.EveryDay
        }
      }
      val monthOfYear = MonthOfYearSchedule.values()(chunks(MOY)(n))
      val hour = chunks(HOUR)(n)
      val minute = chunks(MINUTE)(n)
      val macr0 = KeyFactory.macroKey(chunks(MACR0)(n) + 1)
      val enable = chunks(ENABLE)(n) > 0
      Schedule(key = scheduleKey,
        dow = dow,
        monthOfYear = monthOfYear,
        hour = hour,
        minute = minute,
        macroKey = macr0,
        enabled = enable,
        week = week
      )

    }

  }
}