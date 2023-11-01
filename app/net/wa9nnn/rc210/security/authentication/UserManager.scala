package net.wa9nnn.rc210.security.authentication

import com.typesafe.scalalogging.LazyLogging
import controllers.UserEditDTO
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.util.JsonIoWithBackup
import play.api.libs.json.Json

import java.nio.file.Path

/**
 * Handles persistence of [[User]]s
 * This should only be accessed via [[UserManagerActor]]
 *
 */
class UserManager(usersFile: Path, defaultNoUserLogin: Credentials) extends LazyLogging {
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

  def validate(credentials: Credentials): Option[User] = {
    if (_userRecords.size == 0 && credentials == defaultNoUserLogin) {
      logger.info("No users and default credentials used")
      val dto = UserEditDTO(
        credentials.callsign,
        name = Option("Default Admin"),
        password = Option(credentials.password)
      )
      val user = User(dto)
      Option(user)
    } else
      _userRecords.validate(credentials)
  }

  def remove(id: UserId, who: Who): Unit = {
    _userRecords = _userRecords.remove(id, who)
  }

  def get(id: UserId): Option[User] = {
    users.get(id)
  }
}
