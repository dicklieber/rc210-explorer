package net.wa9nnn.rc210.command

sealed trait ItemValue

case class ItemString(value: String) extends ItemValue {
  override def toString: String = value
}

case class ItemStrings(values: List[String]) extends ItemValue {
  override def toString: String = values.mkString(" ")
}

case class ItemBoolean(value: Boolean) extends ItemValue {
  override def toString: String = value.toString
}

