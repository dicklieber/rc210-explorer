package net.wa9nnn.rc210.data.schedules

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.command.MacroKey

class ScheduleBuilder extends LazyLogging {
  // colXXX index into n2 dimension of array.
  private val colDow = 0
  private val colWeekInMonth = 1
  private val colMoy = 2
  private val colHours = 3
  private val colMinutes = 4
  private val colMacro = 5

  private val array = Array.ofDim[Any](40, 6)

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
        val sDow = nDow.toString
        val chars = sDow.toCharArray
        chars match {
          case Array(dow) =>
            array(setPoint)(colWeekInMonth) = None
            array(setPoint)(colDow) = DayOfWeek.values()(sDow.toInt)
          case Array(wInMo, dow) =>
            array(setPoint)(colWeekInMonth) = Option(wInMo.toString.toInt)
            array(setPoint)(colDow) = DayOfWeek.values()(dow.toString.toInt)
          case x =>
            logger.error(s"DOW must be 1 or 2 chars, got $sDow")
        }
      }
  }

  def putMoy(moys: Seq[Int]): Unit = {
    moys.zipWithIndex
      .foreach { case (moy, setPoint) =>
        array(setPoint)(colMoy) = MonthOfYear.values()(moy)
      }
  }

  def putHours(hours: Seq[Int]): Unit = {
    hours.zipWithIndex
      .foreach { case (hours, setPoint) =>
        array(setPoint)(colHours) = hours
      }
  }

  def putMinutes(minutes: Seq[Int]): Unit = {
    minutes.zipWithIndex
      .foreach { case (minutes, setPoint) =>
        array(setPoint)(colMinutes) = minutes
      }
  }

  def putMacro(macroNumbers: Seq[Int]): Unit = {
    macroNumbers.zipWithIndex
      .foreach { case (macroNumber, setPoint) =>
        array(setPoint)(colMacro) = MacroKey(macroNumber)
      }
  }

  def getSetpointRow(setPoint:Int):Seq[Any] = array(setPoint).toIndexedSeq
}