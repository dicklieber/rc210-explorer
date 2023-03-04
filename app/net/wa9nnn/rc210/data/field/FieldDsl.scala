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


import net.wa9nnn.rc210.key.KeyKindEnum.KeyKind

import scala.language.implicitConversions


package object fieldDefintionSugar {

  /*
    implicit class FieldDefintionSugar(val offset: Int) {
      private var fs: FieldMetadata = FieldMetadata(offset)

      def fn(fieldName: String): FieldDefintionSugar = {
        fs.copy(fieldName = fieldName)
        new FieldDefintionSugar(offset)
      }
    }
  */

  object FieldDefintionSugar {
    implicit def apply(f: (Int, String, KeyKind, String)): FieldMetadata = {
      FieldMetadata(f._1, f._2, f._3, f._4)
    }
  }
}



//object Fd2 {
//
//  import fieldDefintionSugar.FieldDefintionSugar._
//
//  val fields: Seq[FieldMetadata] = Seq(
//
//    (0, "Site Prefix", miscKey, "*2108${value}") % UiDtmf(3),
//    (4, "TT PadTest", portKey, "*2093${value}") % UiDtmf(5),
//
//
//  )
//}

//object Fd2Test extends App {
//  private val head: FieldMetadata = fields.head
//  println(head)
//}


object UiRender extends Enumeration {
  type UiRender = Value
  val checkbox, number, select, dtmfKeys, twoStrings = Value
}