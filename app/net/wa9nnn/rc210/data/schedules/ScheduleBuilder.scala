package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.key.KeyFactory.MacroKey
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.util.MacroSelect

import java.time.LocalTime

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
            val dayOfWeek = previous.dayOfWeek.update(dow.asDigit)
            slots(setPoint) = previous.copy(dayOfWeek = dayOfWeek)
          case Array(wInMo, dow) =>
            val weekInMonth: Option[Int] = Option.when(wInMo != 0)(wInMo)
            slots(setPoint) = previous.copy(weekInMonth = weekInMonth, dayOfWeek = previous.dayOfWeek.update(dow.asDigit))
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
      .foreach { case (newHours, setPoint) =>
        val previous: Schedule = slots(setPoint)

        val newLocalTime: Option[LocalTime] =
          if (newHours < 25) {
            None
          } else {
            val r: Option[LocalTime] = previous.localTime.map { localtime =>
              localtime.withHour(newHours)
            }
            r
          }
        slots(setPoint) = previous.copy(localTime = newLocalTime)
      }
  }

  def putMinutes(minutes: Seq[Int]): Unit = {
    minutes.zipWithIndex
      .foreach { case (newMinute, setPoint) =>
        val previous: Schedule = slots(setPoint)
        val newTime: Option[LocalTime] = previous.localTime
          .map { localtime =>
            localtime.withMinute(newMinute)
          }.orElse(Option(LocalTime.of(1, newMinute)))
        slots(setPoint) = previous.copy(localTime = newTime)
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