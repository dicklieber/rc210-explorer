package net.wa9nnn.rc210.security.authentication

import com.github.andyglow.config.*
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import controllers.UserEditDTO
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.authorzation.AuthFilter.*
import net.wa9nnn.rc210.util.JsonIoWithBackup
import play.api.libs.json.Json
import com.github.andyglow.config.*

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import net.wa9nnn.rc210.util.Configs.*
import os.Path
/**
 * In-memory data store for [[Users]].
 * Handles persistence of [[User]]s
 *
 */
@Singleton()
class UserStore @Inject()(config: Config, defaultNoUsersLogin: DefaultNoUsersLogin) extends LazyLogging {
  private[authentication] val usersFile: Path = config.get[Path]("vizRc210.usersFile")

  private var userMap: TrieMap[UserId, User] = TrieMap.empty

  private[authentication] def readJson = {
    JsonIoWithBackup(usersFile).as[UserRecords]
  }

  load()
  if userMap.isEmpty then
    val defaultCallsign = config.get[String]("vizRc210.authentication.defaultAdmin.callsign")
    val defaultpassword = config.get[String]("vizRc210.authentication.defaultAdmin.password")
    logger.error(s"""No users are configured. Use callsign: "$defaultCallsign" password: "$defaultpassword" to login. """)
    logger.error(s"""Then setup a user.""")

  def load(): Unit =
    try {
      val loaded: UserRecords = readJson
      val builder = TrieMap.newBuilder[UserId, User]
      loaded.users.foreach { user =>
        builder.addOne(user.id -> user)
      }
      userMap = builder.result()
    } catch {
      case _: Exception =>
        logger.error(s"No $usersFile")
    }

  def validate(credentials: Credentials): Option[User] =
    if (userMap.isEmpty && credentials == defaultNoUsersLogin.login) {
      logger.info("No users and default credentials used")
      val dto = UserEditDTO(credentials.callsign, name = Option("Default Admin"), password = Option(credentials.password))
      val user = User(dto)
      Option(user)
    } else
      for {
        entry <- userMap.find(_._2.callsign == credentials.callsign)
        candidate = entry._2
        user <- candidate.validate(credentials)
      } yield {
        user
      }

  def users: Seq[User] =
    userMap.values.toSeq.sorted

  /**
   *
   * @param userEditDTO for new user
   */
  def put(userEditDTO: UserEditDTO)(implicit rcSession: RcSession): Unit = {
    val newEntry = userMap.get(userEditDTO.id) match
      case Some(current) =>
        current.update(userEditDTO)
      case None =>
        User(userEditDTO)

    userMap.update(newEntry.id, newEntry)
    JsonIoWithBackup(usersFile, Json.toJson(userMap))

    save(rcSession.who)
  }

  def remove(userId: UserId)(implicit rcSession: RcSession): Unit =
    userMap.remove(userId)
    save(rcSession.who)

  def get(id: UserId): Option[User] =
    userMap.get(id)

  private def save(who: Who): Unit =
    val userRecords: UserRecords = UserRecords(who = who, users = userMap.values.toSeq.sorted)
    val jsValue = Json.toJson(userRecords)
    JsonIoWithBackup(usersFile, jsValue)

}
