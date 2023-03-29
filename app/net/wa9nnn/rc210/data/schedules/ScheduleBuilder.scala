package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.field.{DayOfWeek, FieldBoolean, FieldTime, WeekInMonth}
import net.wa9nnn.rc210.key.KeyFactory.MacroKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.util.MacroSelect

class ScheduleBuilder extends LazyLogging {
  private val nMax = KeyKind.scheduleKey.maxN()
  val slots = new Array[Schedule](nMax)
  for (setPoint <- 0 until nMax) {
    slots(setPoint) = Schedule.empty(setPoint + 1)
  }

  /**
   * DOW packs one or two fields:
   * From RC-20 Manual:
   * you may alternately use 2 digits for DOW entry and it now becomes DOM (Day Of Month) and consists of 2 digits.
   * The first digit signifies which week within a month to use and the second digit signifies the day of that week to use.
   * For example, if an event is wanted for the 2nd Thursday of every month, you’d enter 24 for the DOW entry.
   *
   * @param nDows 1 or 2 digits. Seq based on setpoints.
   */
  def putDow(nDows: Seq[Int]): Unit = {
    nDows
      .zipWithIndex
      .foreach { case (nDow, setPoint) =>
        val previous: Schedule = slots(setPoint)
        val sDow = nDow.toString
        val chars = sDow.toCharArray
        chars match {
          case Array(dow) =>
            //            slots(setPoint) = leave as None
            val dayOfWeek = DayOfWeek(dow.asDigit)
            slots(setPoint) = previous.copy(dayOfWeek = dayOfWeek)
          case Array(wInMo, dow) =>
            val weekInMonth: WeekInMonth = WeekInMonth(wInMo.asDigit)
            val dayOfWeek: DayOfWeek = DayOfWeek(dow.asDigit)
            slots(setPoint) = previous.copy(weekInMonth = weekInMonth, dayOfWeek = dayOfWeek)
          case x =>
            logger.error(s"DOW must be 1 or 2 chars, got $sDow")
        }
      }
  }

  def putMoy(moys: Seq[Int]): Unit = {
    moys.zipWithIndex
      .foreach { case (moy, setPoint) =>
        val previous: Schedule = slots(setPoint)
        slots(setPoint) = previous.copy(monthOfYear = previous.monthOfYear.update(moy))
      }
  }

  def putHours(hours: Seq[Int]): Unit = {
    hours.zipWithIndex
      .foreach { case (newHours: Int, setPoint) =>
        val previous: Schedule = slots(setPoint)

        if (newHours < 24) {
          val previousTime: FieldTime = previous.time
          val newTime = FieldTime(previousTime.value.withHour(newHours))
          val newSchedule: Schedule = previous.copy(time = newTime, enabled = FieldBoolean(true))
          slots(setPoint) = newSchedule
        } else {
          val newSchedule: Schedule = previous.copy(enabled = FieldBoolean(false))
          slots(setPoint) = newSchedule
        }
      }
  }

  def putMinutes(minutes: Seq[Int]): Unit = {
    minutes.zipWithIndex
      .foreach { case (newMintue: Int, setPoint) =>
        val previous: Schedule = slots(setPoint)
        val time: FieldTime = previous.time
        val newSchedule: Schedule = previous.copy(time = FieldTime(time.value.withMinute(newMintue)))
        slots(setPoint) = newSchedule
      }
  }

  def putMacro(macroNumbers: Seq[Int]): Unit = {
    macroNumbers.zipWithIndex
      .foreach { case (macroNumber, setPoint) =>
        val previous: Schedule = slots(setPoint)
        val macroKey: MacroKey = KeyFactory(KeyKind.macroKey, macroNumber)
        slots(setPoint) = previous.copy(selectedMacroToRun = MacroSelect(macroKey))
      }
  }

}