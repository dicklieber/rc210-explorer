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

/**
 * Metadata for the various [[Key]]s
 */
object KeyKindEnum extends Enumeration {


  def apply(sKeyKind:String): KeyKind = values.find { v =>
    val keyKind = v.asInstanceOf[KeyKind]
    keyKind.prettyName equalsIgnoreCase (sKeyKind)
  }.get.asInstanceOf[KeyKind]
  /**
   *
    * @return all [[KeyKind]]s in al[pha order.
   */
  def keyKinds: Seq[KeyKind] = {
    values
      .toIndexedSeq
      .map(_.asInstanceOf[KeyKind])
      .sorted[KeyKind]
  }
  def namebleKeyKinds: Seq[KeyKind] = {
    keyKinds.filter(_.nameable)
  }

  def createKey(keyKindName:String, number:Int): Key = {
    val maybeValue: Option[KeyKindEnum.Value] = KeyKindEnum.values.find(_.asInstanceOf[KeyKind].matches(keyKindName))
    maybeValue.map(v =>
      v.asInstanceOf[KeyKind]
        .instantiate(number)

    ).getOrElse{
      println(keyKindName)
      throw new IllegalArgumentException(s"Can't make Key for KeyKind: $keyKindName Number: $number")
  }

  }

  /**
   * Provides metadata about the [[Key]] types
   *
   * @param maxN numbered key instances can go from 1 from 1 to maxN,
   * @param instantiate create an instance of the Key for this [[KeyKind]] and number.
   */
  case class KeyKind(maxN: Int, instantiate: Int => Key, nameable:Boolean = true) extends super.Val with Ordered [Value]{
    def matches(keyKindName: String): Boolean = {
     keyKindName == prettyName
    }


    override def compareTo(that: KeyKindEnum.Value): Int = toString() compareTo that.toString

    def prettyName:String = toString()
      .dropRight(3)
      .capitalize

    def apply[T](number: Int): T = {
      instantiate(number).asInstanceOf[T]
    }
  }

  val alarmKey: KeyKind = KeyKind(5,  AlarmKey)
  val dtmfMacroKey: KeyKind = KeyKind(195, DtmfMacroKey, nameable = false)
  val courtesyToneKey: KeyKind = KeyKind(10, CourtesyToneKey)
  val functionKey: KeyKind = KeyKind(1005, FunctionKey, nameable = false)
  val macroKey: KeyKind = KeyKind(105, MacroKey)
  val messageMacroKey: KeyKind = KeyKind(90, MessageMacroKey)
  val miscKey: KeyKind = KeyKind(1, MiscKey)
  val wordKey: KeyKind = KeyKind(256, WordKey, nameable = false)
  val portKey: KeyKind = KeyKind(3, PortKey)
  val scheduleKey: KeyKind = KeyKind(40, ScheduleKey)

}



