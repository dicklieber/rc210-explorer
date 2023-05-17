package net.wa9nnn.rc210.security.authentication

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.{Cell, Row}
import controllers.UserEditDTO
import net.wa9nnn.rc210.security.RcRole.viewRole
import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who.Callsign
import net.wa9nnn.rc210.security.{RcRole, UserId, Who}
import org.mindrot.jbcrypt.BCrypt
import play.api.i18n.{Messages, MessagesProvider}
import play.api.libs.json.{Format, Json}

/**
 *
 * @param callsign of user.
 * @param name     friendly name.
 * @param hash     bcrypt hash.
 * @param email    address
 * @param role     what this user can do.
 * @param id       uniqueID for this user. Allows changing any other field.
 */
case class UserRecord(callsign: Callsign,
                      name: String,
                      email: String,
                      hash: String,
                      role: RcRole = viewRole,
                      id: UserId = UserId())
  extends Ordered[UserRecord] with LazyLogging {
  def tempAdmin: Option[UserRecord] = {
    if (role == RcRole.tempAdminRole)
      Option(this)
    else
      None
  }


  def toRow()(implicit messagesProvider: MessagesProvider): Row = {
    Row.ofAny(
      Cell("edit")
      //        .withImage(com.wa9nnn.allstarmgr.security.authentication.UserRecord.edituserIcon)
      //        .withUrl(routes.AuthenticatationController.editUser(id).url)
      ,
      callsign,
      name,
      Cell(email)
        .withUrl(s"mailto:$email"),
      Cell(Messages(role.name))
    )
  }


  def validate(plainText: String): Option[UserRecord] = {
    if (BCrypt.checkpw(plainText, hash))
      Option(this)
    else
      None

  }

  override def compare(that: UserRecord): Int = {
    this.callsign compare that.callsign
  }

  def userEditDTO: UserEditDTO = {
    controllers.UserEditDTO(callsign, name, email, role, id)
  }

  /**
   *
   * @param in wht ot change
   * @return a copy of modified [[UserRecord]]
   */
  def update(in: UserEditDTO): UserRecord = {
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
    new UserRecord(in.callsign, in.name, in.email, newHash, in.role, id)
  }

  lazy val toWho: Who = Who(callsign, id, email)

}

object UserRecord extends App {
  /**
   * create a new [[UserRecord]]
   *
   * @param in initial data.
   * @return
   */
  def apply(in: UserEditDTO): UserRecord = {
    assert(in.password.nonEmpty, "Must have a password to create a new UserRecord.")
    val password = in.password.get
    val hash = BCrypt.hashpw(password, BCrypt.gensalt())
    new UserRecord(callsign = in.callsign, name = in.name, email = in.email, hash = hash, role = in.role, id = in.id)
  }

  lazy val headerCells = Seq("Edit", "Callsign", "Name", "e-mail", "Roles")
  lazy val edituserIcon = "/assets/images/icons8-edit_file.png"

  //  lazy val initalUserEditor = AmSubject(CallsignCell(Callsign("WA9NNN")), id.randomid(), "Dick", "wa9nnn@arrl.net", List(AmRole.adminRole))
  implicit val fmtUserRecord: Format[UserRecord] = Json.format[UserRecord]
}

