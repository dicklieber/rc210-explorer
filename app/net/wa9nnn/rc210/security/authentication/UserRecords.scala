package net.wa9nnn.rc210.security.authentication

import com.typesafe.scalalogging.LazyLogging
import controllers.UserEditDTO
import play.api.libs.json.{Format, Json}

import java.time.Instant

/**
 * Immutable compete set of user information.
 *
 * @param who               who changed this.
 * @param users             all the user data.
 * @param stamp             when.
 */
case class UserRecords(who: Who = Who(), users: List[User] = List.empty, stamp: Instant = Instant.now()) extends LazyLogging {
  private lazy val idMap: Map[UserId, User] = {
    users.map(u => u.id -> u).toMap
  }
  private lazy val callSignMap: Map[Callsign, User] = {
    users.map(u => u.callsign -> u).toMap
  }

  def size: Int = users.size

  /**
   * get [[User]]
   *
   * @param credentials from browser
   * @return Some[User] if callsign exists and password hash matches.
   */
  def validate(credentials: Credentials): Option[User] = {
    for {
      userRecord <- callSignMap.get(credentials.callsign.toUpperCase())
      ur <- userRecord.validate(credentials.password)
    } yield {
      ur
    }
  }

  def get(id: UserId): Option[User] = {
    idMap.get(id)
  }

  /**
   * insert or replace a user [[User]]
   *
   * @param who is making this change.
   * @return a new [[UserRecords]]
   */
  def update(in: UserEditDTO, who: Who): UserRecords = {

    val maybeRecord: Option[User] = idMap.get(in.id)

    val maybeUserRecord: Option[User] = maybeRecord.map(currentUserReord => currentUserReord.update(in))
    val userRecord = maybeUserRecord.getOrElse(User(in))

    finish(idMap + (userRecord.id -> userRecord), who)
  }

  /**
   * remove a user [[com.wa9nnn.allstarmgr.security.authentication.UserRecord]]
   *
   * @param uuid      new or changed.
   * @param who       is making this change.
   * @return a new [[UserRecords]]
   */
  def remove(uuid: UserId, who: Who): UserRecords = {
    finish(idMap - uuid, who)
  }

  // common when changing anything
  private def finish(uuidMap: Map[UserId, User], who: Who): UserRecords = {
    val updatedUsers = uuidMap.values
      .toList
      .sorted
    UserRecords(who, updatedUsers)
  }

  def iterator: Iterator[User] = users.iterator
}

object UserRecords {
  implicit val fmtUserRecords: Format[UserRecords] = Json.format[UserRecords]
}