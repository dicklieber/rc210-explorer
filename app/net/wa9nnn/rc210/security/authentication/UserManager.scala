package net.wa9nnn.rc210.security.authentication
import com.github.andyglow.config._
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import controllers.UserEditDTO
import net.wa9nnn.rc210.io.DatFile
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.{RcRole, Who}
import net.wa9nnn.rc210.util.JsonIoWithBackup
import play.api.libs.json.Json

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

/**
 * Handles persistence of [[com.wa9nnn.allstarmgr.security.authentication.UserRecord]]s
 *
 * @param varDirectory where all writeable files live.
 * @param config       to get allstar.authentication.defaultAdmin
 */
@Singleton
class UserManagerImpl @Inject()(config: Config)(implicit datFile: DatFile) extends UserManager with LazyLogging {

  private var _userRecords: UserRecords = UserRecords()
  private val usersFile: Path = datFile.usersFile

  def load(): Unit = {

    try {
      _userRecords = JsonIoWithBackup(usersFile).as[UserRecords]
    } catch {
      case _: Exception =>
        logger.error(s"No ${usersFile.toFile}")
    }
  }

  load()

  def needsAdminUser: Boolean = {
    _userRecords.needsAdminUser
  }

  override def userRecords: UserRecords = _userRecords

  override def put(userDetailData: UserEditDTO)(implicit who: Who): Unit = {
    _userRecords = _userRecords.update(userDetailData)
    JsonIoWithBackup(usersFile, Json.toJson(_userRecords))
  }

  override def validate(login: Login): Option[UserRecord] = {
    _userRecords.validate(login).orElse {
      defaultAdmin(login)
    }
  }

  /**
   * See if no admins and the Login attempt is for the default admin user.
   *
   * @param login being attempted.
   */
  private def defaultAdmin(login: Login): Option[UserRecord] = {
    val defaultAdmin: Config = config.getConfig("vizRc210.authentication.defaultAdmin")
    for {
      defaultCallsign <- defaultAdmin.get[Option[String]]("callsign")
      defaultPassword <- defaultAdmin.get[Option[String]]("password")
      if defaultCallsign == login.callsign.toString
      if defaultPassword == login.password
      if _userRecords.needsAdminUser
    } yield {
      val tempAdmin = UserRecord(controllers.UserEditDTO(callsign = login.callsign, role = RcRole.tempAdminRole, password = Option("none")))
      logger.info("login with {}", tempAdmin)
      tempAdmin
    }
  }

  override def delete(id: UserId)(implicit who: Who): Unit = {
    _userRecords = _userRecords.remove(id)
  }

  override def get(id: UserId): Option[UserRecord] = {
    userRecords.get(id)
  }

}

trait UserManager {
  def needsAdminUser: Boolean

  def userRecords: UserRecords

  def validate(login: Login): Option[UserRecord]

  def put(userDetailData: UserEditDTO)(implicit who: Who): Unit

  def get(id: UserId): Option[UserRecord]

  def delete(id: UserId)(implicit who: Who): Unit
}

