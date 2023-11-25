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

package net.wa9nnn.rc210.util.select

import com.typesafe.scalalogging.LazyLogging
import enumeratum.*

/**
 * for example:
 * {{{
 *  object Occurrence extends EnumValue[Occurrence]
 * }}}
 *
 * @tparam T
 */
trait EnumValue[T <: EnumEntryValue] extends PlayEnum[T] with Selections with LazyLogging:
  val values: IndexedSeq[T]

  override def options: Seq[EnumEntryValue] = values

  /**
   * @param target find the Enum with this rc210Value.
   * @return
   */
  def find(target: Int): T =
    values.find {
      _.rc210Value == target
    } match
      case Some(value) => value
      case None =>
        val sValues = values.map(t => s"${t.rc210Value}: ${t.toString}").mkString(",")
        val head = values.head
        logger.error(s"No $target in $sValues! Using $head")
        head

trait Selections:
  def options: Seq[EnumEntryValue]

/**
 * for example:
 * {{{
 *   sealed trait Occurrence(val rc210Value: Int) extends EnumEntryValue
 * }}}
 *
 * Most uses of this are toString and rc210Value
 */
trait EnumEntryValue extends EnumEntry:
  val rc210Value: Int
  /**
   * This allow get all the values from any [[EnumEntryValue]]. Enables typed-based html form fields.
   */
  val values: IndexedSeq[?]


  
  
