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

package net.wa9nnn.rc210.key

import com.wa9nnn.util.tableui.{Cell, CellProvider}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.named.NamedKeySource
import net.wa9nnn.rc210.key.KeyFactory.Key.nameForKey
import play.twirl.api.Html

/**
 * All keys live here
 * They are all created at startup.
 * Instances can only be obtained by calling methods on the object.
 */
object KeyFactory {
  def remoteBaseKey: RemoteBaseKey = RemoteBaseKey()


  private val keys: Seq[Key] = {
    {
      for {
        keyKind <- KeyKind.values().toIndexedSeq
        number <- 1 to keyKind.maxN
      } yield {
        keyKind match {
          case KeyKind.logicAlarmKey => LogicAlarmKey(number)
          case KeyKind.meterKey => MeterKey(number)
          case KeyKind.meterAlarmKey => MeterAlarmKey(number)
          case KeyKind.dtmfMacroKey => DtmfMacroKey(number)
          case KeyKind.courtesyToneKey => CourtesyToneKey(number)
          case KeyKind.functionKey => FunctionKey(number)
          case KeyKind.macroKey => MacroKey(number)
          case KeyKind.messageKey => MessageKey(number)
          case KeyKind.commonKey => CommonKey()
          case KeyKind.portKey => PortKey(number)
          case KeyKind.scheduleKey => ScheduleKey(number)
          case KeyKind.timerKey => TimerKey(number)
          case KeyKind.clockKey => ClockKey(number)
          case KeyKind.remoteBaseKey => RemoteBaseKey(number)
        }
      }
    }
  }

  private val map: Map[String, Key] = keys.map { k =>
    k.toString -> k
  }.toMap

  lazy val kindAndCounts: Seq[(KeyKind, Int)] = {
    keys
      .groupBy(_.kind)
      .map { case (kind, keys) =>
        kind -> keys.length
      }.toSeq
      .sortBy(_._1)

  }

  def apply[K <: Key](sKey: String): K = {
    map(sKey).asInstanceOf[K]
  }

  def apply[K <: Key](keyKind: KeyKind, number: Int): K = {
    assert(number != 0, "Keys cannot have number 0!")

    apply(keyKind.skey(number))
  }

  val availableKeys: Seq[Key] = {
    keys
  }

  def apply[K <: Key](keyKind: KeyKind): Seq[K] = {
    availableKeys
      .filter(_.kind.eq(keyKind))
      .map(_.asInstanceOf[K])
  }

  def functionKey(number: Int): FunctionKey = apply(KeyKind.functionKey, number)

  def macroKey(number: Int): MacroKey = apply(KeyKind.macroKey, number)

  def dtmfMacroKey(number: Int): DtmfMacroKey = apply(KeyKind.dtmfMacroKey, number)

  def portKey(number: Int): PortKey = apply(KeyKind.portKey, number)


  def timerKey(number: Int): TimerKey = apply(KeyKind.timerKey, number)

  def logicAlarmKey(number: Int): LogicAlarmKey = apply(KeyKind.logicAlarmKey, number)

  def meterKey(number: Int): MeterKey = apply(KeyKind.meterKey, number)

  def meterAlarmKey(number: Int): MeterAlarmKey = apply(KeyKind.meterAlarmKey, number)

  def messageKey(number: Int): MessageKey = apply(KeyKind.messageKey, number)

  def scheduleKey(number: Int): ScheduleKey = apply(KeyKind.scheduleKey, number)

  def commonKey(number: Int): CommonKey = apply(KeyKind.commonKey, number)

  def commonKey(): CommonKey = apply(KeyKind.commonKey, 1)

  def clockKey: ClockKey = apply(KeyKind.clockKey, 1)

  lazy val defaultMacroKey: MacroKey = apply(KeyKind.macroKey, 1)


  /**
   *
   * @param kind metadata about a kind (or flavor of a [[Key]].
   * @param number
   */
  sealed abstract class Key(val kind: KeyKind, val number: Int) extends CellProvider with Ordered[Key] {

    val name: String = kind.toString

    def fieldKey(fieldName: String): FieldKey = FieldKey(fieldName, this)

    val prettyName: String = kind.toString.dropRight(3).capitalize

    override val toString: String = s"$name$number"


    override def toCell: Cell = {
      val name = nameForKey(this)
      val c = if (name.isEmpty)
        Cell(number)
      else
        Cell(s"$number: $name")
      c.withCssClass(kind.toString)
    }


    def keyWithName: String = {
      s"$number ${nameForKey(this)}"
    }

    def userDefinedName: String = nameForKey(this)

    /**
     * Display the Key. Number: <input>
     * This can be placed in a [[com.wa9nnn.util.tableui.Row]].
     *
     * @param name for name attribute in <input>
     */
    def namedCell(param: String = fieldKey("name").param): Cell = {
      val html: Html = views.html.fieldNamedKey(this, nameForKey(this), param)
      Cell.rawHtml(html.toString())
    }

    def namedHtml: String = {
      val html: Html = views.html.fieldNamedKey(this, nameForKey(this), "name")
      html.toString()
    }

    def noEdit: String = {
      s"$number: ${nameForKey(this)}"
    }

    /**
     * Replace's 'n' in the template with the number (usually a port number).
     *
     * @param template in
     * @return with 'n' replaced by the port number.
     */
    def replaceN(template: String): String = {
      template.replaceAll("n", number.toString)
    }


    override def compare(that: Key): Int = {
      var ret = kind compareTo that.kind
      if (ret == 0)
        ret = number compareTo that.number
      ret
    }
  }

  object Key {

    def setNamedSource(namedsource: NamedKeySource): Unit = {
      if (_namedSource.isDefined) throw new IllegalStateException("NamedSource already set.")
      _namedSource = Option(namedsource)
    }

    protected var _namedSource: Option[NamedKeySource] = None

    def nameForKey(key: Key): String =
      _namedSource.map(_.nameForKey(key)).getOrElse("")
  }

  case class PortKey protected(override val number: Int) extends Key(KeyKind.portKey, number)

  case class LogicAlarmKey(override val number: Int) extends Key(KeyKind.logicAlarmKey, number)

  case class MeterKey(override val number: Int) extends Key(KeyKind.meterKey, number)

  case class MeterAlarmKey(override val number: Int) extends Key(KeyKind.meterAlarmKey, number)

  case class MacroKey private(override val number: Int) extends Key(KeyKind.macroKey, number)

  case class MessageKey(override val number: Int) extends Key(KeyKind.messageKey, number)

  case class FunctionKey(override val number: Int) extends Key(KeyKind.functionKey, number)

  case class ScheduleKey(override val number: Int) extends Key(KeyKind.scheduleKey, number)


  case class DtmfMacroKey(override val number: Int) extends Key(KeyKind.dtmfMacroKey, number)

  case class CourtesyToneKey(override val number: Int) extends Key(KeyKind.courtesyToneKey, number)

  /**
   * There can be any number of [[CommonKey]] but they don't index into a map by themselves. MaxN just indicates how many to extract for a given rc2input name.
   */
  case class CommonKey(override val number: Int = 1) extends Key(KeyKind.commonKey, number)

  case class TimerKey(override val number: Int = 1) extends Key(KeyKind.timerKey, number)

  case class ClockKey(override val number: Int = 1) extends Key(KeyKind.clockKey, number)

  case class RemoteBaseKey(override val number: Int = 1) extends Key(KeyKind.remoteBaseKey, number)
}