package net.wa9nnn.rc210.security.authentication

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.StaticConfigs.maxSessionCookieAge
import net.wa9nnn.rc210.security.Who.Callsign
import net.wa9nnn.rc210.security.authentication.RcSession.SessionId
import net.wa9nnn.rc210.security.authentication.SessionManager.playSessionName
import play.api.libs.json.{Format, Json}
import play.api.mvc.Cookie

import java.time.Instant

/**
 *
 */
case class RcSession(sessionId: SessionId,
                   user: User,
                   started: Instant = Instant.now()) extends Ordered[RcSession] {
  def callsign: Callsign = user.callsign

  var touched: Instant = started

  def cookie: Cookie = Cookie(name = playSessionName,
    value = sessionId,
    maxAge = maxSessionCookieAge,
    httpOnly = true)


  def touch(): RcSession = {
    touched = Instant.now()
    this
  }



  override def compare(that: RcSession): Int = this.user.callsign compareTo that.user.callsign
}

object RcSession extends LazyLogging {
  type SessionId = String
  implicit val fmtSession: Format[RcSession] = Json.format[RcSession]
}