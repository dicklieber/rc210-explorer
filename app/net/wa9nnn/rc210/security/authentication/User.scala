package net.wa9nnn.rc210.security.authentication

import com.typesafe.scalalogging.LazyLogging
import controllers.UserEditDTO
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who.Callsign
import net.wa9nnn.rc210.security.{UserId, Who}
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.{Format, Json}

/**
 *
 * @param callsign of user.
 * @param name     friendly name.
 * @param hash     bcrypt hash.
 * @param email    address
 * @param id       uniqueID for this user. Allows changing any other field.
 */
case class User(callsign: Callsign,
                name: String,
                email: String,
                hash: String,
                id: UserId = UserId())
  extends Ordered[User] with LazyLogging {


  def validate(plainText: String): Option[User] = {
    if (BCrypt.checkpw(plainText, hash))
      Option(this)
    else
      None

  }

  override def compare(that: User): Int = {
    this.callsign compare that.callsign
  }

  def userEditDTO: UserEditDTO = {
    controllers.UserEditDTO(callsign, name, email, id)
  }

  /**
   *
   * @param in wht ot change
   * @return a copy of modified [[User]]
   */
  def update(in: UserEditDTO): User = {
    assert(in.id == id, "Attempt to update wrong id!")
    val newHash = in.password match {
      case Some(newPwd) =>
        val r = BCrypt.hashpw(newPwd, BCrypt.gensalt())
        logger.trace("New hash for {} {} {} to {}", id, callsign, newPwd, r)
        r
      case None =>
        logger.trace("Using existing hash for {} {}", id, callsign)
        hash
    }
    new User(in.callsign, in.name, in.email, newHash, id)
  }

  lazy val who: Who = Who(callsign, id, email)

}

object User {
  /**
   * create a new [[User]]
   *
   * @param in initial data.
   * @return
   */
  def apply(in: UserEditDTO): User = {
    assert(in.password.nonEmpty, "Must have a password to create a new UserRecord.")
    val password = in.password.get
    val hash = BCrypt.hashpw(password, BCrypt.gensalt())
    new User(callsign = in.callsign, name = in.name, email = in.email, hash = hash, id = in.id)
  }

  implicit val fmtUserRecord: Format[User] = Json.format[User]
}

