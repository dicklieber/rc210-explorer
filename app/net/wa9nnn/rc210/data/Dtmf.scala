package net.wa9nnn.rc210.data

import play.api.libs.json._

/**
 * Parsed from Seq[Int] with two dtmf chars packed into each int.
 *
 * @param value consisting of only dtmf digits.
 */
case class Dtmf(value: String = "") {
  val enabled: Boolean = value.nonEmpty
  override def toString: String = value
}

object Dtmf {
  // Note the RC210 binary can't encode 'D'
  val dtmfDigits = "x1234567890*#ABC"

  def apply(in: Seq[Int]): Dtmf = {
    assert(in.length == 5, "Dtmf fields are 5 ints long")

    val digits: Seq[Char] = in.flatMap { int =>
      Seq(int & 0x0f, (int & 0xf0) >> 4)
    }.takeWhile(_ != 0)
      .map(dtmfDigits(_)) // convert to dtmf keys
    new Dtmf(new String(digits.toArray))
  }

  implicit val fmtKey: Format[Dtmf] = new Format[Dtmf] {
    override def reads(json: JsValue): JsResult[Dtmf] = {

      try {
        JsSuccess(Dtmf(json.toString()))
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(dtmf: Dtmf): JsValue = {
      JsString(dtmf.toString)
    }
  }
}
