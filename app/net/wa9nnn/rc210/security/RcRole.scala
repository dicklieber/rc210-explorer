package net.wa9nnn.rc210.security

import be.objectify.deadbolt.scala.models.Role
import play.api.data.FormError
import play.api.i18n.{Messages, MessagesProvider}
import play.api.libs.json.{Format, Json}

/**
 *
 * @param name
 * @param displayName
 * @param tooltip
 */
case class RcRole(name: String, displayName: String, tooltip: String) extends Role {
  def selectButton()(implicit messages: MessagesProvider): (String, String) = {
    name -> messages.messages(displayName)
  }

  override def toString: String = {
    name
  }

  def displayNameL10N()(implicit messagesProvider: MessagesProvider): String = Messages(displayName)

}

object RcRole {
  val tempAdminRole: RcRole = RcRole("admin", "role.admin.name", "role.admin.tooltip")
  val adminRole: RcRole = RcRole("admin", "role.admin.name", "role.admin.tooltip")
  val viewRole: RcRole = RcRole("view", "role.view.name", "role.view.tooltip")
  val roles: Seq[RcRole] = List(viewRole, adminRole)
  private val map = roles.map(r => r.name -> r).toMap

  import play.api.data.format.Formats._
  import play.api.data.format.Formatter

  implicit object amRoleFormatter extends Formatter[RcRole] {
    override val format: Option[(String, Nil.type)] = Some(("format.role", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], RcRole] = {
      parsing(RcRole(_), "error.role", Nil)(key, data)
    }

    override def unbind(key: String, amRole: RcRole): Map[String, String] = Map(key -> amRole.name)
  }

  implicit val fmtRcRole: Format[RcRole] = Json.format[RcRole]
  def apply(id: String): RcRole = {
    map(id)
  }


}



