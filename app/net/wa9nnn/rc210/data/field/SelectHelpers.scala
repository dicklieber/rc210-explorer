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

package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.{Key, KeyFormats}

import java.time.LocalTime


object FormHelpers {
  //  implicit def form2String(name: String)(implicit map: Map[String, Seq[String]]): String = {
  //    map(name).head
  //  }

  def form2OptInt(name: String)(implicit map: Map[String, Seq[String]]): Option[Int] = {
    val option: Option[String] = map(name).headOption
    val value: Option[Int] = option.map { s => s.toInt }
    value
  }

  def form2FieldKey(name: String)(implicit map: Map[String, Seq[String]]): FieldKey = {
    val value: Seq[String] = map(name)
    FieldKey.fromParam(value.head)
  }

  def form2Key[T <: Key](name: String)(implicit map: Map[String, Seq[String]]): T = {
    val value: Seq[String] = map(name)
    KeyFormats.parseString(value.head).asInstanceOf[T]
  }

  def form2OptTime(name: String)(implicit map: Map[String, Seq[String]]): Option[LocalTime] = {
    val value: Seq[String] = map(name)
    for {
      value: String <- map(name)
      if !value.isEmpty
    } yield {
      LocalTime.parse(value)
    }
  }.headOption
}

object SelectEnumerationHelper {
  def apply(enumeration: Enumeration, name: String)(implicit map: Map[String, Seq[String]]): enumeration.Value = {
    val value: Seq[String] = map(name)
    enumeration.withName(value.head)
  }

  /**
   * Generate HTML for an [[Enumeration]]
   *
   * @param enumeration for field.
   * @param current     it's current value.
   * @param param       will be key for POSTed data.
   * @return
   */
  def apply[T](enumeration: Enumeration, current: String, param: String): String = {
    val options: Seq[SelectOption] = enumeration.values.toSeq.map { e: enumeration.Value =>
      val opt = SelectOption(e.toString, e.toString)
      if (e.toString == current)
        opt.select
      else
        opt
    }
    views.html.fieldSelect(param, options).toString()
  }
}


object SelectKeyHelper {
  /**
   * Generate an Html string.
   *
   * @param current key, will be selected in <select>
   * @param param   will be key for POSTed data.
   * @return the HtmL fragment for this field.
   */
  def apply(current: Key, param: String): String = {

    val keys: Seq[Key] = current.kind.allKeys
    val options = keys.map { k: Key =>
      val opt = SelectOption(k.toString, k.toString)
      if (k == current)
        opt.select
      else
        opt
    }
    views.html.fieldSelect(param, options).toString()
  }

  def apply[T](name: String)(implicit map: Map[String, Seq[String]]): T = {
    val value: Seq[String] = map(name)
    KeyFormats.parseString(value.head).asInstanceOf[T]
  }


}
