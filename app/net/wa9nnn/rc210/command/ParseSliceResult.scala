package net.wa9nnn.rc210.command

import net.wa9nnn.rc210.serial.Slice

/**
 * Usually constructred using a [[ParseResultBuilder]].
 *
 * @param slice  in an [[net.wa9nnn.rc210.serial.Memory]]
 * @param values pasrsed from the slice.
 * @param errors any error encountered.
 */
case class ParseSliceResult(slice: Slice,
                            values: Seq[ItemValue] = Seq.empty,
                            errors: Seq[ItemError] = Seq.empty) {
  override def toString: String = {
    s"$slice :\n\t" +
      values.map(_.toString).mkString("\r\t")
  }
}

case class ParseResultBuilder(slice: Slice) {
  private val itemBuilder = Seq.newBuilder[ItemValue]
  private val errorBuilder = Seq.newBuilder[ItemError]

  def apply(itemValue: ItemValue): ParseResultBuilder = {
    itemBuilder += itemValue
    this
  }

  def apply(itemProblem: ItemError): ParseResultBuilder = {
    errorBuilder += itemProblem
    this
  }

  def result: ParseSliceResult = ParseSliceResult(
    slice,
    itemBuilder.result(),
    errorBuilder.result())
}


object ParseSliceResult {
  def apply(slice: Slice, itemValue: ItemValue): ParseSliceResult = {
    new ParseSliceResult(slice, Seq(itemValue))
  }
}