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

package net.wa9nnn.rc210.ui

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.key.KeyFactory.Key

import scala.reflect.ClassTag

/**
 * Creates a [[Cell]] with <select>
 * and parses the value from the form back into an Enum.
 *
 * @param name   to be used as the name of the <select> element Gets combined with [[Key]] and [[FieldKey]]
 * @tparam E the Enum type.
 */

class EnumSelect[E <: Enum[E] : ClassTag](name: String) extends LazyLogging{

  val clazz: Class[E] = implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]
  val values: Array[E] = clazz.getEnumConstants

  private val options: Seq[String] = values.map(_.toString)

  def fromOrdinal(ordinal:Int): E = {
    values(ordinal)
  }


  def toCell(current: E)(implicit key: Key): Cell = {
    val html = toHtml(current)
    Cell.rawHtml(html)
  }

   def toHtml(current: E)(implicit key: Key) = {
    val param = FieldKey(name, key).param
    views.html.fieldSelect(param, current.toString, options.toIndexedSeq).toString()
  }

  def toCell()(implicit key: Key): Cell = {
    val param = FieldKey(name, key).param
    val html = views.html.fieldSelect(param, "", options).toString()
    Cell.rawHtml(html)
  }

  def fromForm(in: String): E = {
    Enum.valueOf(clazz, in)
  }

  def fromKv()(implicit kv:Map[String,String], key: Key): E = {
    val param = FieldKey(name, key).param
    try {
      fromForm(kv(param))
    } catch {
      case e:Exception =>
        logger.error(s"name: $name param: $param", e)
        throw e
    }
  }
}





