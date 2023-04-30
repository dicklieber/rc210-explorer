package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.field.{DayOfWeek, DowBase, MonthOfYear, Week}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.serial.Memory.Chunk

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
    // Each chunk is one RC-210 memory setpoint field.
    implicit val chunks: Seq[Chunk] = memory.chunks(616, KeyKind.scheduleKey.maxN(), fieldsInRC210Schedule)


    for {
      n <- 0 until KeyKind.scheduleKey.maxN()
    } yield {
      val key = KeyFactory.scheduleKey(n + 1)
      val dow: DowBase = {dowBuilder(chunks(DOW)(n))}
      val monthOfYear = MonthOfYear.values()(chunks(MOY)(n))
      val hour = chunks(HOUR)(n)
      val minute = chunks(MINUTE)(n)
      val macr0 = KeyFactory.macroKey(chunks(MACR0)(n) + 1)
      val enable = chunks(ENABLE)(n) > 0
      Schedule(key = key,
        dow = dow,
        monthOfYear = monthOfYear,
        hour = hour,
        minute = minute,
        macroKey = macr0,
        enabled = enable
      )

    }

  }

   def dowBuilder(dow:Int): DowBase = {
    val sDow = dow.toString
    sDow.toCharArray match {
      case Array(dow: Char) =>
        DayOfWeek.values()(dow.asDigit)
      case Array(wInMo, dow) =>
        WeekAndDow(Week.values()(wInMo.asDigit), DayOfWeek.values()(dow.asDigit))
      case _ =>
        logger.error(s"DOW must be 1 or 2 chars, got $sDow")
        DayOfWeek.EveryDay
    }
  }
}