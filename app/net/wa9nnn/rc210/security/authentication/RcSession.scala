package net.wa9nnn.rc210.security.authentication

import be.objectify.deadbolt.scala.models.{Permission, Subject}
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.StaticConfigs.maxSessionCookieAge
import net.wa9nnn.rc210.security.authentication.RcSession.SessionId
import net.wa9nnn.rc210.security.authentication.SessionManager.playSessionName
import net.wa9nnn.rc210.security.{RcRole, Who}
import play.api.libs.json.{Format, Json}
import play.api.mvc.Cookie

import java.time.Instant

/**
 *
 */
case class RcSession(sessionId: SessionId,
                     who: Who,
                     roles: List[RcRole],
                     remoteAddress: String,
                     started: Instant = Instant.now())
  extends Subject with Ordered[RcSession] {
  var touched: Instant = started

  def cookie: Cookie = Cookie(name = playSessionName,
    value = sessionId,
    maxAge = maxSessionCookieAge,
    httpOnly = true)


  def touch(): RcSession = {
    touched = Instant.now()
    this
  }


  override def identifier: String = who.id


  override def permissions: List[Permission] = List.empty // Not using permissions, just Roles.

  /**
   *
   * @param amRoles zero or more roles. No roles always returns true.
   * @return true if this has, at least, one of the [[RcRole]]. No arg returns true.
   */
  def hasRole(amRoles: RcRole*): Boolean = {
    amRoles.isEmpty || roles.intersect(amRoles).nonEmpty
  }

  def filterRole[T](amRoles: RcRole*)(block: () => T): Option[T] = {
    Option.when(hasRole(amRoles: _*))(
      block()
    )
  }

  override def compare(that: RcSession): Int = this.who compareTo that.who
}

object RcSession extends LazyLogging {

  def hasRole[T](roles: RcRole*)(block: () => T)(implicit maybeAmSession: Option[RcSession]): Option[T] = {
    val maybeT = for {
      amSession <- maybeAmSession
      if amSession.hasRole(roles: _*)
    } yield {
      block()
    }
    if (maybeT.isEmpty)
      logger.error("Not authorized")
    maybeT
  }

  type SessionId = String
  implicit val fmtSession: Format[RcSession] = Json.format[RcSession]
}