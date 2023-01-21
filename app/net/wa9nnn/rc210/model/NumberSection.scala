package net.wa9nnn.rc210.model

case class NumberSection(number: Int, items: List[DataItem])

object NumberSection {
  def apply(items: List[DataItem]): NumberSection = {
    val numbers: Set[Int] = items.foldLeft(Set.empty[Int]) { case (accum, item) => accum + item.number }
    if (numbers.size != 1)
      throw new IllegalArgumentException(s"Items in a section must all have the same number. But have: ${numbers.mkString(",")}")

    new NumberSection(numbers.head, items)
  }
}
