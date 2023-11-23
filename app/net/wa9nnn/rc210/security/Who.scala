package net.wa9nnn.rc210.security

import play.api.libs.json.{Json, OFormat}


case class Who(callsign: Callsign = "unknown", id: UserId = none, email: String = "") extends Ordered[Who] {
  override def compare(that: Who): Int = this.callsign compareTo that.callsign
}

object Who {
  implicit val whoFmt: OFormat[Who] = Json.format
  type Callsign = String
}
