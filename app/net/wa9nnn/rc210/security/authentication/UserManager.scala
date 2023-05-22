package net.wa9nnn.rc210.security.authentication

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import controllers.UserEditDTO
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.util.JsonIoWithBackup
import play.api.libs.json.Json

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

/**
 * Handles persistence of [[User]]s
 *
 * @param datFile      where all writeable files live.
 * @param config       to get allstar.authentication.defaultAdmin
 */
@Singleton
class UserManager @Inject()(config: Config)(implicit datFile: DatFile) extends LazyLogging {

  private var _userRecords: UserRecords = UserRecords()
  private val usersFile: Path = datFile.usersFile

  private val defaultNoUserLogin: Login = Login(
    callsign = config.getString("vizRc210.authentication.defaultAdmin.callsign"),
    password = config.getString("vizRc210.authentication.defaultAdmin.password")
  )

  def load(): Unit = {

    try {
      _userRecords = JsonIoWithBackup(usersFile).as[UserRecords]
    } catch {
      case _: Exception =>
        logger.error(s"No ${usersFile.toFile}")
    }
  }

  load()

  def userRecords: UserRecords = _userRecords

  def put(userDetailData: UserEditDTO)(implicit who: Who): Unit = {
    _userRecords = _userRecords.update(userDetailData)
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
      val user =  User(dto)
      Option(user)
    } else
      _userRecords.validate(login)
  }

  def delete(id: UserId)(implicit who: Who): Unit = {
    _userRecords = _userRecords.remove(id)
  }

  def get(id: UserId): Option[User] = {
    userRecords.get(id)
  }

}
