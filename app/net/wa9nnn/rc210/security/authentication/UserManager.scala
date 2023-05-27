package net.wa9nnn.rc210.security.authentication

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import configs.syntax._
import controllers.UserEditDTO
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.util.JsonIoWithBackup
import play.api.libs.json.Json

import java.nio.file.Path

/**
 * Handles persistence of [[User]]s
 * This shoulsd only be access via [[UserManagerActor]]
 *
 */
class UserManager(config: Config) extends LazyLogging {
  private val usersFile: Path = config.get[Path]("vizRc210.usersFile").value
  private val defaultNoUserLogin: Login = Login(
    callsign = config.getString("vizRc210.authentication.defaultAdmin.callsign"),
    password = config.getString("vizRc210.authentication.defaultAdmin.password")
  )
  private var _userRecords: UserRecords = UserRecords()

  def load(): Unit = {

    try {
      _userRecords = JsonIoWithBackup(usersFile).as[UserRecords]
    } catch {
      case _: Exception =>
        logger.error(s"No ${usersFile.toFile}")
    }
  }

  load()

  def users: UserRecords = _userRecords

  /**
   *
   * @param userDetailData for new user
   * @param user           who is doing this.
   */
  def put(userDetailData: UserEditDTO, user: User): Unit = {
    _userRecords = _userRecords.update(userDetailData, user.who)
    JsonIoWithBackup(usersFile, Json.toJson(_userRecords))
  }

  def validate(login: Login): Option[User] = {
    if (_userRecords.size == 0 && login == defaultNoUserLogin) {
      logger.info("No users and default credentials used")
      val dto = UserEditDTO(
        login.callsign,
        name = Option("Default Admin"),
        password = Option(login.password)
      )
      val user = User(dto)
      Option(user)
    } else
      _userRecords.validate(login)
  }

  def remove(id: UserId, who: Who): Unit = {
    _userRecords = _userRecords.remove(id, who)
  }

  def get(id: UserId): Option[User] = {
    users.get(id)
  }

}
