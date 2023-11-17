package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.util.{SelectItemNumber, Selectable}
import play.api.data.FormError
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter

sealed trait MonthOfYearDST(val rc210Value: Int, val display: String) extends SelectItemNumber

object MonthOfYearDST extends Selectable[MonthOfYearDST]

case object January extends MonthOfYearDST(1, "January")

case object February extends MonthOfYearDST(2, "February")

case object March extends MonthOfYearDST(3, "March")

case object April extends MonthOfYearDST(4, "April")

case object May extends MonthOfYearDST(5, "May")

case object June extends MonthOfYearDST(6, "June")

case object July extends MonthOfYearDST(7, "July")

case object August extends MonthOfYearDST(8, "August")

case object September extends MonthOfYearDST(9, "September")

case object October extends MonthOfYearDST(10, "October")

case object November extends MonthOfYearDST(11, "November")

case object December extends MonthOfYearDST(12, "December")
