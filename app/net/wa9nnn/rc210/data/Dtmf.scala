package net.wa9nnn.rc210.data

import play.api.libs.json.*

/*/**
 * Parsed from Seq[Int] with two dtmf chars packed into each int.
 *
 * @param value consisting of only dtmf digits.
 */
case class Dtmf(value: String = "") {
  val enabled: Boolean = value.nonEmpty

  override def toString: String = value
}*/

object Dtmf {
  type Dtmf = String

  // Note the RC210 binary can't encode 'D'
  val dtmfDigits = "x1234567890*#ABC"

  def fromMemoryInts(in: Seq[Int]): Option[Dtmf] = {

    val digits: Seq[Char] = in.flatMap { int =>
      Seq(int & 0x0f, (int & 0xf0) >> 4)
    }.takeWhile(_ != 0)
      .map(int => {
        val c: Char = dtmfDigits(int)
        c
      }) // convert to dtmf keys
    val str = new String(digits.toArray)
    Option.when(str.nonEmpty) {
      new Dtmf(str)
    }

  }
}
