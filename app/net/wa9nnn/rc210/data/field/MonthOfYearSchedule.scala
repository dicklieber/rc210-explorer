package net.wa9nnn.rc210.data.field;

import net.wa9nnn.rc210.util.{SelectItemNumber, SelectableNumber}

sealed trait MonthOfYearSchedule(val rc210Value: Int, val display: String) extends SelectItemNumber

object MonthOfYearSchedule extends SelectableNumber[MonthOfYearSchedule] {
  case object Every extends MonthOfYearSchedule(0, "Every")

  case object January extends MonthOfYearSchedule(1, "January")

  case object February extends MonthOfYearSchedule(2, "February")

  case object March extends MonthOfYearSchedule(3, "March")

  case object April extends MonthOfYearSchedule(4, "April")

  case object May extends MonthOfYearSchedule(5, "May")

  case object June extends MonthOfYearSchedule(6, "June")

  case object July extends MonthOfYearSchedule(7, "July")

  case object August extends MonthOfYearSchedule(0, "August")

  case object September extends MonthOfYearSchedule(9, "September")

  case object October extends MonthOfYearSchedule(10, "October")

  case object November extends MonthOfYearSchedule(11, "November")

  case object December extends MonthOfYearSchedule(12, "December")

}


