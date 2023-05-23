package net.wa9nnn.rc210.security.authentication

import akka.actor.ActorSystem
import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.authentication.RcSession.SessionId
import net.wa9nnn.rc210.security.authentication.SessionManager.sessionIdGenerator
import net.wa9nnn.rc210.util.JsonIoWithBackup
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, Json}
import play.api.mvc.Cookie

import java.io.FileNotFoundException
import java.math.BigInteger
import java.nio.file.Path
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 * Creates and manages [[RcSession]] objects.
 * The RcSessionId in an [[RcSession]] is stored as a cookie, via play RcSession support in the client.
 * Periodically Persisted if dirty.
 * Loaded at startup
 */
@Singleton
class SessionManager @Inject()(config: Config, datFile: DatFile, actorSystem: ActorSystem) extends LazyLogging {

  private val sessionMap = new TrieMap[SessionId, RcSession]
  private val userMap = new TrieMap[UserId, RcSession]
  private var dirty = false
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  actorSystem.scheduler.scheduleWithFixedDelay(5 seconds, 10 seconds) { () => purge() }
  private val sessionFile: Path = datFile.sessionFile

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
      logger.error("Error loading file: {}!", sessionFile)
  }

  logger.info("Session Manager Started")

  def create(user: User)(implicit messagesProvider: MessagesProvider): RcSession = {
    val newRcSession = userMap.getOrElseUpdate(user.id, {
      val lSession = sessionIdGenerator.nextLong()
      val bytes: Array[Byte] = Base64.getEncoder.encode(BigInteger.valueOf(lSession).toByteArray)
      val sessionId = new SessionId(bytes)
      val newRcSession: RcSession = RcSession(sessionId = sessionId, user = user)
      sessionMap.put(newRcSession.sessionId, newRcSession)
      userMap.put(user.id, newRcSession)
      newRcSession
    }

    )
    dirty = true
    newRcSession
  }

  def lookup(cookie: Cookie): Option[RcSession] = lookup(cookie.value)

  def lookup(sessionId: SessionId): Option[RcSession] = {
    sessionMap.get(sessionId)
      .map { rcSession =>
        rcSession.touch()
        dirty = true
        rcSession
      }
  }

  def remove(sessionId: SessionId): Unit = {
    sessionMap.remove(sessionId)
      .foreach { RcSession: RcSession =>
        userMap.remove(RcSession.user.id)
        dirty = true
      }
  }

  def purge(): Unit = {
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
      logger.debug("Wrote {} RcSessions to {}", sessions.size, sessionFile.toFile.toURI.toString)
      dirty = false
    }
  }

  def sessions: Seq[RcSession] = {
    sessionMap.values.toSeq.sorted
  }
}

object SessionManager {
  val sessionIdGenerator = new SecureRandom()
  type RcSessionId = String
  val playSessionName = "rcSession"
}

case class RcSessions(sessions: Seq[RcSession]) {
  def size: Int = sessions.size

}

object RcSessions {
  implicit val fmtRcSessions: Format[RcSessions] = Json.format[RcSessions]
}