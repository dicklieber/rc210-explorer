package net.wa9nnn.rc210.security.authentication

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.StaticConfigs.maxSessionCookieAge
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.Who.Callsign
import net.wa9nnn.rc210.security.authentication.RcSession.{SessionId, playSessionName}
import play.api.libs.json.{Format, Json}
import play.api.mvc.Cookie

import java.math.BigInteger
import java.security.SecureRandom
import java.time.{Duration, Instant}
import java.util.Base64
import scala.reflect.ScalaLongSignature

/**
 *
 */

/**
 * One authenticated user session.
 * Note [[touched]] makes this mutable, but should otherwise be treated as immutable.
 *
 * @param sessionId what gets stored in a cookie on the browser.
 * @param user      who started this session.
 * @param remoteId where this came from.
 * @param started   when it was started.
 */
case class RcSession(sessionId: SessionId,
                     user: User,
                     remoteIp: String,
                     started: Instant = Instant.now()) extends Ordered[RcSession] with RowSource {

  def callsign: Callsign = user.callsign

  var touched: Instant = started
  def who:Who = user.who

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

  override def toString: SessionId = s"Session Callsign: ${user.callsign} from $remoteIp"
}

object RcSession extends LazyLogging {
  type SessionId = String

  val noSession:RcSession = RcSession(
    sessionId = "none",
    user = User(callsign = "", hash = ""),
    remoteIp = ""
    )
  
  def apply(user: User, remoteIp:String): RcSession = {
    val lSession = sessionIdGenerator.nextLong()
    val bytes: Array[Byte] = Base64.getEncoder.encode(BigInteger.valueOf(lSession).toByteArray)
    val sessionId = new SessionId(bytes)


    new RcSession(sessionId, user, remoteIp)
  }

  implicit val fmtSession: Format[RcSession] = Json.format[RcSession]

  def header(count: Int): Header = {
    Header(s"Sessions ($count)", "Callsign", "Name", "Started", "Touched", "Age")
  }

  private  val sessionIdGenerator = new SecureRandom()
  val playSessionName: String = "rcSession"

}