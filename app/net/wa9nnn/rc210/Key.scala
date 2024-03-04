/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.Key.nameForKey
import net.wa9nnn.rc210.KeyKind.*
import net.wa9nnn.rc210.{NamedKey, NamedKeySource}
import net.wa9nnn.rc210.ui.{EnumEntryValue, MacroSelect}
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.*
import play.api.mvc.PathBindable

/**
 * Identifies various RC210 objects.
 *
 * @param keyKind     of the Key
 * @param rc210Value  0 is a magic number used for things like [[KeyKind.CommonKey]]
 */
case class Key(keyKind: KeyKind, override val rc210Value: Int = 0) extends Ordered[Key] with EnumEntryValue {
  def check(expected: KeyKind): Unit = if (expected != keyKind) throw IllegalArgumentException(s"Expecting Key of type $expected, but got $this}")

  assert(rc210Value <= keyKind.maxN, s"Max number for $keyKind is ${keyKind.maxN}")

  override def toString: String = s"${keyKind.entryName}$rc210Value"

  override def compare(that: Key): Int =
    var ret = keyKind.toString compare that.keyKind.toString
    if (ret == 0)
      ret = rc210Value compareTo that.rc210Value
    ret

  def namedKey: NamedKey = NamedKey(this, nameForKey(this))

  def keyWithName: String =
    val name = Key.nameForKey(this)
    s"$rc210Value: $name"

  /**
   * Replaces 'n' in the template with the number (usually a port number).
   *
   * @param template in
   * @return with 'n' replaced by the port number.
   */
  def replaceN(template: String): String = {
    template.replaceAll("n", rc210Value.toString)
  }

  override def values: IndexedSeq[EnumEntryValue] =
    throw new NotImplementedError() //Not needed as we override options

  override def options: Seq[(String, String)] = MacroSelect.options
}

object Key extends LazyLogging:
  val keyName: String = "key"
  private val kparser = """(\D+)(\d*)""".r

  def apply(maybeSKey: Option[String]): Option[Key] =
    maybeSKey.map(Key(_))

  def apply(sKey: String): Key =
    try
      sKey match
        case kparser(sKind, sNumber) =>
          val keyKind = KeyKind.withName(sKind)
          new Key(keyKind, sNumber.toInt)
        case _ =>
          throw new IllegalArgumentException(s"""Can't parse "$sKey"!""")
    catch
      case e: Exception =>
        throw e

  def setNamedSource(namedSource: NamedKeySource): Unit = {
    if (_namedSource.isDefined)
      logger.warn("NamedSource already set. Bad if not in test.")
    _namedSource = Option(namedSource)
  }

  implicit val fmtKey: Format[Key] = new Format[Key] {
    override def reads(json: JsValue): JsResult[Key] = {
      val sKey = json.as[String]
      try {
        JsSuccess(apply(sKey))
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(sak: Key): JsValue = {
      JsString(sak.toString)
    }
  }

  private var _namedSource: Option[NamedKeySource] = None

  def nameForKey(key: Key): String =
    _namedSource.map(_.nameForKey(key)).getOrElse("")

  private def keys(keyKind: KeyKind): Seq[Key] =
    for {
      number <- 1 to keyKind.maxN
    } yield {
      Key(keyKind, number)
    }

  lazy val portKeys: Seq[Key] = keys(Port)
  lazy val macroKeys: Seq[Key] = keys(Macro)
  lazy val timerKeys: Seq[Key] = keys(Timer)
  val commonkey: Key = Key(KeyKind.Common)

  /**
   * Codec to allow non-string types i routes.conf definitions.
   */
  implicit def keyKindPathBinder: PathBindable[KeyKind] = new PathBindable[KeyKind] {
    override def bind(key: String, value: String): Either[String, KeyKind] = {
      Right(KeyKind.withName(value))
    }

    override def unbind(key: String, macroKey: KeyKind): String = {
      macroKey.toString
    }
  }

  import play.api.data.format.Formats.*

  implicit val keyFormatter: Formatter[Key] = new Formatter[Key]:
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Key] =
      parsing(Key(_), s"BadKey $key", Nil)(key, data)

    override def unbind(key: String, value: Key): Map[String, String] = Map(key -> value.toString)





