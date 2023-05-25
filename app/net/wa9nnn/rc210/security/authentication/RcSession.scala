package net.wa9nnn.rc210.security.authentication

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.StaticConfigs.maxSessionCookieAge
import net.wa9nnn.rc210.security.Who.Callsign
import net.wa9nnn.rc210.security.authentication.RcSession.SessionId
import net.wa9nnn.rc210.security.authentication.SessionManager.playSessionName
import play.api.libs.json.{Format, Json}
import play.api.mvc.Cookie

import java.math.BigInteger
import java.security.SecureRandom
import java.time.{Duration, Instant}
import java.util.Base64

/**
 *
 */

/**
 * One authennticated user session.
 * Note [[touched]] makes this mutable, but should otherwise be treated as immutable.
 *
 *
 * @param sessionId what gets stored in a cookie on the browser.
 * @param user      who started this session.
 * @param started   when it was started.
 */
case class  RcSession(sessionId: SessionId,
                     user: User,
                     started: Instant = Instant.now()) extends Ordered[RcSession] with RowSource {

  def callsign: Callsign = user.callsign

  var touched: Instant = started

  def cookie: Cookie = Cookie(name = playSessionName,
    value = sessionId,
    maxAge = maxSessionCookieAge,
    httpOnly = true)


  def touch(): Unit = {
    touched = Instant.now()
  }


  override def compare(that: RcSession): Int = this.user.callsign compareTo that.user.callsign

  override def toRow: Row = Row.ofAny(
    user.callsign,
    user.name,
    started,
    touched,
    Duration.between(started, Instant.now())
  )

}

object RcSession extends LazyLogging {
  type SessionId = String

  def apply(user: User): RcSession = {
    val lSession = sessionIdGenerator.nextLong()
    val bytes: Array[Byte] = Base64.getEncoder.encode(BigInteger.valueOf(lSession).toByteArray)
    val sessionId = new SessionId(bytes)


    new RcSession(sessionId, user)
  }

  implicit val fmtSession: Format[RcSession] = Json.format[RcSession]

  def header(count: Int): Header = {
    Header(s"Sessions ($count)", "Callsign", "Name", "Started", "Touched", "Age")
  }

  private val sessionIdGenerator = new SecureRandom()
  val playSessionName: SessionId = "rcSession"

}