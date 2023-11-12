package net.wa9nnn.rc210.security

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.field.Formatters
import net.wa9nnn.rc210.data.remotebase.RemoteBase
import org.apache.commons.lang3.Conversion
import play.api.data.FormError
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter

import java.util.{Base64, UUID}

/**
 * A permanent User ID that can survice Callsign and name changes.
 */
object UserId {
  def unapply(u: UserId): Option[(String, UserId)] = Some(u)

  private val encoder: Base64.Encoder = Base64.getEncoder

  /**
   * Create a compact, url-safe, representation of a UUID.
   *
   * @return
   */
  def apply(): UserId = {
    val uuid = UUID.randomUUID
    val bytes = Conversion.uuidToByteArray(uuid, new Array[Byte](16), 0, 16)
    encoder.encodeToString(bytes)
  }

  val none: UserId = ""

  type UserId = String

  implicit object MacroKeyFormatter extends Formatter[UserId] with LazyLogging {
    override val format: Option[(String, Nil.type)] = Some(("format..UserId", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], UserId] = {
      parsing(s => {
        try {
          s
        } catch {
          case e: Exception =>
            logger.error(s"Parsing $s to UserId!")
            throw e
        }
      }, "error.UserId", Nil)(key, data)
    }

    override def unbind(key: String, value: UserId): Map[String, String] = Map(key -> value.toString)
  }


}
