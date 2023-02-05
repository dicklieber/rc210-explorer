package net.wa9nnn.rc210.command

import play.api.libs.json.{Json, OFormat}

import scala.math.Ordered.orderingToOrdered


case class CommandId(base: String, port: Option[Int] = None, sub: Option[Int] = None) extends Ordered[CommandId] {
  override def toString: String = {
    val sPort = port.map(p => s" port: $p").getOrElse("")
    val sSub = sub.map(s => s" sub: $s").getOrElse("")

    s"$base$sPort$sSub"
  }

  override def compare(that: CommandId): Int = {
    var ret = base compareTo (that.base)
    if (ret == 0)
      ret = port compareTo (that.port)
    if (ret == 0)
      ret = sub compareTo (that.sub)
    ret
  }
}

object CommandId {
  def apply(base: String, port: Int, sub: Int): CommandId = {
    new CommandId(base, Option(port), Option(sub))
  }

  implicit val cmdIdFmt: OFormat[CommandId] = Json.format[CommandId]
}