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
import com.wa9nnn.wa9nnnutil.tableui.{Cell, CellProvider}
import net.wa9nnn.rc210.KeyMetadata.*
import net.wa9nnn.rc210.ui.NamedKeyManager
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.*
import play.api.mvc.PathBindable

/**
 * Primary key for a [[net.wa9nnn.rc210.data.field.FieldValue]] stored in
 * the[[net.wa9nnn.rc210.data.datastore.DataStore]] or JSON.
 *
 * @param keyMetadata     some
 * @param rc210Number     e.g. port or Schedule number
 * @param qualifier       used with [[KeyMetadata.Common]]
 * @param indicator       indicates this key refers to the name of the  [[net.wa9nnn.rc210.data.field.FieldValue]].
 *                        Only of interest when the [[Key]] is used in a [[PathBindable]]; between an HTML form and code.
 *                        This should only be [[KeyIndicator.Value]] in datastore or JSON.
 */
case class Key(keyMetadata: KeyMetadata,
               rc210Number: Option[Int] = None,
               qualifier: Option[String] = None,
               indicator: KeyIndicator = KeyIndicator.iValue)
  extends CellProvider with LazyLogging:
  rc210Number.foreach(n =>
    assert(n <= keyMetadata.maxN, s"Max number for $keyMetadata is ${keyMetadata.maxN}")
  )

  def check(expected: KeyMetadata): Unit =
    if (expected != keyMetadata) throw IllegalArgumentException(s"Expecting Key of type $expected, but got $this}")

  val display: String =
    val sNumber = rc210Number.map(_.toString).getOrElse("")
    val sQualifier = qualifier.map(q => s";$q").getOrElse("")
    s"${keyMetadata.entryName}$sNumber$sQualifier"

  /**
   *
   * @return used in JSON or HRML form names.
   */
  def id: String =
    s"${indicator.char}${keyMetadata.entryName}|${rc210Number.getOrElse("")}|${qualifier.getOrElse("")}"

  def namedKey: NamedKey =
    NamedKey(this, name)

  /**
   * @return current name for this key
   */
  def name: String =
    NamedKeyManager.nameForKey(this)

  /**
   * Replaces 'n' in the template with the number (usually a port number).
   *
   * @param template in
   * @return with 'n' replaced by the port number.
   */
  def replaceN(template: String): String =
    rc210Number.map(number =>
      template.replaceAll("n", number.toString)
    ).getOrElse(template)

  def toCell: Cell =
    Cell(toString)

  override def toString: String =
    s"""${keyMetadata.entryName}${rc210Number.getOrElse("")}${qualifier.map(q => s":$q").getOrElse("")}"""

  def keyWithName: String =
    s"${rc210Number.getOrElse(throw new IllegalStateException("Named keys must have a number1"))}: $name"

  def withIndicator(keyIndicator: KeyIndicator): Key =
    copy(indicator = keyIndicator)

  def number: Int =
    rc210Number.getOrElse(throw new IllegalStateException("No number in this Key"))

object Key extends LazyLogging:
  given Ordering[Key] with
    def compare(x: Key, y: Key): Int = {
      // Compare by keyMetadata first (assuming it has an ordering defined)
      val metadataComparison = implicitly[Ordering[KeyMetadata]].compare(x.keyMetadata, y.keyMetadata)
      if (metadataComparison != 0) return metadataComparison

      // Then compare by rc210Number (if present)
      val rcComparison = Ordering.Option(Ordering.Int).compare(x.rc210Number, y.rc210Number)
      if (rcComparison != 0) return rcComparison

      // Finally compare by qualifier
      Ordering.Option(Ordering.String).compare(x.qualifier, y.qualifier)
    }

  val keyName: String = "key"

  private val keyRegx = """([|nk])(.*)\|(\d{0,4})\|(.*)""".r

  def fromId(id: String): Key =
    id match
      case keyRegx(sIndicator, sKK, sNumber, sQualifier) =>
        val keyMetadata = KeyMetadata.withName(sKK)
        val maybeNumber = Option.when(sNumber.nonEmpty)(sNumber.toInt)
        val maybeQualifier = Option.when(sQualifier.nonEmpty)(sQualifier)
        val indicator: KeyIndicator = KeyIndicator.from(sIndicator)
        Key(keyMetadata, maybeNumber, maybeQualifier, indicator)
      case x =>
        throw new IllegalArgumentException(s"No match for $x")

  def apply(keyMetadata: KeyMetadata, number: Int): Key =
    Key(keyMetadata, Some(number))

  def apply(keyMetadata: KeyMetadata, number: Int, qualifier: String): Key =
    Key(keyMetadata, Some(number), Some(qualifier))

  def apply(keyMetadata: KeyMetadata, qualifier: String): Key =
    Key(keyMetadata, qualifier = Some(qualifier))

  def apply(maybeSKey: Option[String]): Option[Key] =
    maybeSKey.map(fromId)

  implicit val fmtKey: Format[Key] = new Format[Key] {
    override def reads(json: JsValue): JsResult[Key] = {
      val sKey = json.as[String]
      try
      {
        JsSuccess(fromId(sKey))
      }
      catch
      {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(key: Key): JsValue = {
      JsString(key.id)
    }
  }

  private def keys(keyKind: KeyMetadata): Seq[Key] =
    for
    {
      number <- 1 to keyKind.maxN
    } yield
    {
      Key(keyKind, number)
    }

  lazy val portKeys: Seq[Key] = keys(Port)
  lazy val macroKeys: Seq[Key] = keys(Macro)
  lazy val commonkey: Key = Key(KeyMetadata.Common)

  /**
   * Codec to allow non-string types i routes.conf definitions.
   */
  implicit val keyKindPathBinder: PathBindable[Key] = new PathBindable[Key] {
    override def bind(paramKey: String, value: String): Either[String, Key] = {
      Right(Key.fromId(value))
    }

    override def unbind(paramKey: String, key: Key): String = {
      key.id
    }
  }

  import play.api.data.format.Formats.*

  implicit val keyFormatter: Formatter[Key] = new Formatter[Key]:
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Key] =
      parsing(fromId, s"BadKey $key", Nil)(key, data)

    override def unbind(key: String, value: Key): Map[String, String] =
      Map(key -> value.toString)




