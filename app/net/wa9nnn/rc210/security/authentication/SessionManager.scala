package net.wa9nnn.rc210.security.authentication

import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import com.typesafe.config.Config

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.security.authentication.RcSession.SessionId
import net.wa9nnn.rc210.util.JsonIoWithBackup
import play.api.libs.json.{Format, Json}
import play.api.mvc.Cookie

import java.io.FileNotFoundException
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.language.postfixOps

/**
 * Creates and manages [[RcSession]] objects.
 * The RcSessionId in an [[RcSession]] is stored as a cookie, via play RcSession support in the client.
 * Periodically Persisted if dirty.
 * Loaded at startup
 */
class SessionManager(sessionFile: Path) extends LazyLogging {


  private val sessionMap = new TrieMap[SessionId, RcSession]
  private var dirty = false


  try {
    val sessions = JsonIoWithBackup(sessionFile).as[RcSessions].sessions
    sessions.foreach { session =>
      sessionMap.put(session.sessionId, session)
      logger.debug("Loaded {} RcSessions from {}", sessionMap.size, sessionFile)
    }
  } catch {
    case _: FileNotFoundException =>
      logger.info("Session file: {} not found!", sessionFile)
    case e: Exception =>
      logger.error("Error loading file: {} {}!", sessionFile, e.getMessage)
  }

  logger.info("Session Manager Started")

  private def removeAnyExistingSession(user: User): Unit = {
    val sessionsForUser = sessionMap.filter { case (_, session) => session.user == user }
    sessionsForUser
      .foreach { case (sessionId, _) =>
        val maybeRemoved = sessionMap.remove(sessionId)
        maybeRemoved.foreach { session =>
          logger.trace("Removed session: {}", session.toString)
        }
      }
  }


  def create(user: User, ip: String): RcSession = {
    removeAnyExistingSession(user)
    dirty = true
    setupSession(user, ip)
  }

  private def setupSession(user: User, remoteIp: String): RcSession = {
    val newRcSession = RcSession(user, remoteIp)
    sessionMap.put(newRcSession.sessionId, newRcSession)
    newRcSession
  }

  def lookup(cookie: Cookie): Option[RcSession] = {
    lookup(cookie.value)
  }

  def lookup(sessionId: SessionId): Option[RcSession] = {
    sessionMap.get(sessionId)
      .map { rcSession =>
        rcSession.touch()
        dirty = true
        logger.trace("Found sessionId: {}", sessionId)
        rcSession
      }
  }

  def remove(sessionId: SessionId): Unit = {
    sessionMap.remove(sessionId) match {
      case Some(removedSession) =>
        logger.debug(s"Session: $sessionId removed.", removedSession)
      case None =>
        logger.error(s"Session: $sessionId did not exist! ")
    }

    dirty = true
  }

  /**
   * Invoke periodically.
   *
   * [[SessionManager]] will purge stale sessions and write if dirty.
   */
  def tick(): Unit = {
    logger.trace("RcSession Purge")
    val removeOlderThan = Instant.now().minus(3, ChronoUnit.HOURS)
    val beforePurge = sessionMap.size
    sessionMap.filterInPlace { (_, RcSession) =>
      RcSession.touched isAfter removeOlderThan
    }
    if (beforePurge != sessionMap.size) {
      dirty = true
    }
    if (dirty) {
      val sessions = RcSessions(sessionMap.values.toList)
      JsonIoWithBackup(sessionFile, Json.toJson(sessions))
      logger.debug("Wrote {} RcSessions to {}", sessions.size, sessionFile.toFile.toURI.toString)
      dirty = false
    }
  }

  def sessions: Seq[RcSession] = {
    sessionMap.values.toSeq.sorted
  }
}

object SessionManager {
  val playSessionName = "rcSession"
}

case class RcSessions(sessions: Seq[RcSession]) {
  def size: Int = sessions.size

}

object RcSessions {
  implicit val fmtRcSessions: Format[RcSessions] = Json.format[RcSessions]
}

