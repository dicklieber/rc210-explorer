package net.wa9nnn.rc210.security.authentication

import com.typesafe.scalalogging.LazyLogging
import controllers.UserEditDTO
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who.Callsign
import net.wa9nnn.rc210.security.{RcRole, Who}
import play.api.libs.json.{Format, Json}

import java.time.Instant

/**
 * Immutable compete set of user information.
 *
 * @param who               who changed this.
 * @param users             all the user data.
 * @param stamp             when.
 */
case class UserRecords(who: Who = Who(), users: List[UserRecord] = List.empty,  val stamp: Instant = Instant.now()) extends LazyLogging {

  lazy val idMap: Map[UserId, UserRecord] = users.map(u => u.id -> u).toMap
  lazy val callSignMap: Map[Callsign, UserRecord] = users.map(u => u.callsign -> u).toMap

  def size: Int = users.size

  def needsAdminUser: Boolean = {
    try {
      val map = idMap
      !map.values.exists { userRecord =>
        userRecord.role == RcRole.adminRole
      }
    } catch {
      case e: Exception =>
        logger.error("Looks for admin", e)
        true
    }
  }


//  def toTable()(implicit messagesProvider: MessagesProvider): Table = {
//    val topHeader = s"Users (${users.length})"
//    val header = Header(topHeader, headerCells: _*)
//    Table(header, users.map(_.toRow()))
//  }


  def validate(login: Login): Option[UserRecord] = {

    for {
      userRecord <- callSignMap.get(login.callsign)
      ur <- userRecord.validate(login.password)
    } yield {
      ur
    }
  }

  def get(id: UserId): Option[UserRecord] = {
    idMap.get(id)
  }

  /**
   * insert or replace a user [[UserRecord]]
   *
   * @param who is making this change.
   * @return a new [[UserRecords]]
   */
  def update(in: UserEditDTO)(implicit who: Who): UserRecords = {

    val maybeRecord: Option[UserRecord] = idMap.get(in.id)

    val maybeUserRecord: Option[UserRecord] = maybeRecord.map(currentUserReord => currentUserReord.update(in))
    val userRecord = maybeUserRecord.getOrElse(UserRecord(in))

    finish(idMap + (userRecord.id -> userRecord), who)
  }

  /**
   * remove a user [[com.wa9nnn.allstarmgr.security.authentication.UserRecord]]
   *
   * @param uuid      new or changed.
   * @param who       is making this change.
   * @return a new [[com.wa9nnn.allstarmgr.security.authentication.UserRecords]]
   */
  def remove(uuid: UserId)(implicit who: Who): UserRecords = {
    finish(idMap - uuid, who)
  }

  // common when changing anything
  private def finish(uuidMap: Map[UserId, UserRecord], who: Who): UserRecords = {
    val updatedUsers = uuidMap.values
      .toList
      .sorted
    UserRecords(who, updatedUsers)
  }
}

object UserRecords {
  implicit val fmtUserRecords: Format[UserRecords] = Json.format[UserRecords]
}