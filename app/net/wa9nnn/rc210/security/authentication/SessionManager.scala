package net.wa9nnn.rc210.security.authentication

import akka.actor.ActorSystem
import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.authentication.SessionManager.{SessionId, sessionIdGenerator}
import net.wa9nnn.rc210.util.JsonIoWithBackup
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, Json}
import play.api.mvc.Cookie

import java.io.FileNotFoundException
import java.net.InetAddress
import java.nio.file.Path
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 * Creates and manages [[RcSession]] objects.
 * The SessionId in an [[RcSession]] is stored as a cookie, via play session support in the client.
 * Periodically Persisted if dirty.
 * Loaded at startup
 */
@Singleton
class SessionManager @Inject()(config: Config, datFile: DatFile, actorSystem: ActorSystem) extends LazyLogging {

  private val sessionMap = new TrieMap[SessionId, RcSession]
  private val uuidMap = new TrieMap[UserId, RcSession]
  private var dirty = false
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  actorSystem.scheduler.scheduleWithFixedDelay(5 seconds, 10 seconds) { () => purge() }
  private val sessionFile: Path = datFile.sessionFile

  try {
    val sessions = JsonIoWithBackup(sessionFile).as[Sessions]
    sessions.sessions.foreach { session =>
      sessionMap.put(session.sessionId, session)
      logger.debug("Loaded {} sessions from {}", sessionMap.size, sessionFile)
    }
  } catch {
    case _: FileNotFoundException =>
      logger.info("Session file: {} not found!", sessionFile)
    case e: Exception =>
      logger.error("Error loading file: {}!", sessionFile, e)

  }


  def create(userRecord: UserRecord, remoteAddress: InetAddress)(implicit messagesProvider: MessagesProvider): RcSession = {
    val uuid = userRecord.id
    val newSession = uuidMap.getOrElseUpdate(uuid, {
      val newSession: RcSession = RcSession(sessionId = sessionIdGenerator.nextLong().toString,
        who = userRecord.toWho,
        roles = List(userRecord.role),
        remoteAddress = remoteAddress.toString)
      sessionMap.put(newSession.sessionId, newSession)
      newSession
    })
    dirty = true
    newSession
  }

  def lookup(cookie: Cookie): Option[RcSession] = lookup(cookie.value)

  def lookup(sessionId: SessionId): Option[RcSession] = {
    sessionMap.get(sessionId)
      .map { session =>
        session.touch()
        dirty = true
        session
      }
  }

  def remove(sessionId: SessionId): Unit = {
    sessionMap.remove(sessionId)
      .foreach { session: RcSession =>
        uuidMap.remove(session.who.id)
        dirty = true
      }
  }

  def purge(): Unit = {
    logger.trace("Session Purge")
    val removeOlderThan = Instant.now().minus(3, ChronoUnit.HOURS)
    val beforePurge = sessionMap.size
    sessionMap.filterInPlace { (_, session) =>
      session.touched isAfter removeOlderThan
    }
    uuidMap.filterInPlace { (_, session) =>
      session.touched isAfter removeOlderThan
    }
    if (beforePurge != sessionMap.size) {
      dirty = true
    }
    if (dirty) {
      val sessions = Sessions(sessionMap.values.toList)
      logger.debug("Wrote {} sessions to {}", sessions.sessions.size, sessionFile.toFile.toURI.toString)
      dirty = false
    }
  }



}

object SessionManager {
  val sessionIdGenerator = new SecureRandom()
  type SessionId = String
  val playSessionName = "rcsession"
}

case class Sessions(sessions: List[RcSession])

object Sessions {
  implicit val fmtSessions: Format[Sessions] = Json.format[Sessions]
}