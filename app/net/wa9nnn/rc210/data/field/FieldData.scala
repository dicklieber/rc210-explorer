/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */

package net.wa9nnn.rc210.data.field

import net.wa9nnn.rc210.*
import play.api.libs.json.{Format, Json}
import play.libs.F
import net.wa9nnn.rc210.data.field.FieldValue.fmt

/**
 * Immutable state for a [[FieldKey]]. 
 *
 * @param fieldKey   what it's for.
 * @param fieldValue current value.
 * @param candidate  next value
 */
case class FieldData(fieldKey: FieldKey, fieldValue: FieldValue, candidate: Option[FieldValue] = None):
  /**
   * 
   * @return current or candidate value.
   */
  def value[T <: FieldValue]: FieldValue =
    candidate.getOrElse(fieldValue).asInstanceOf[T]

  /**
   *
   * @param newFieldValue already parsed to a [[FieldValue]]
   * @return updated [[FieldEntry]].
   */
  def setCandidate(newFieldValue: FieldValue): FieldData =
    if fieldValue == newFieldValue then
      copy(candidate = None)
    else
      copy(candidate = Option(newFieldValue))

  def acceptCandidate(): FieldData =
    copy(
      candidate = None,
      fieldValue = candidate.getOrElse(fieldValue)
    )

  def clearCandidate: FieldData =
    copy(candidate = None)

object FieldData:
  implicit val ordering: Ordering[FieldData] =
    Ordering.by[FieldData, FieldKey](_.fieldKey)
  implicit val fmt: Format[FieldData] = Json.format[FieldData]
