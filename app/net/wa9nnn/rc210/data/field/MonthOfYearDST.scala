package net.wa9nnn.rc210.data.field

import play.api.data.FormError
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter

enum MonthOfYearDST {
  case January,
  February,
  March,
  April,
  May,
  June,
  July,
  August,
  September,
  October,
  November,
  December

  def number: Int = {
    ordinal + 1
  }

  import play.api.data.format.Formats._
  import play.api.data.format.Formatter



}

implicit object MonthOfYearDSTFormatter extends Formatter[MonthOfYearDST] {
  
  override val format: Option[(String, Seq[Any])] = Some(("format.moyDst", Nil))

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], MonthOfYearDST] = parsing(MonthOfYearDST.valueOf, "error.moy", Nil)(key, data)

  override def unbind(key: String, value: MonthOfYearDST): Map[String, String] = Map(key -> value.toString)
}