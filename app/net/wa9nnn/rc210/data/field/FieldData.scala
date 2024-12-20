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

import net.wa9nnn.rc210.Key
import play.api.libs.json.{Json, OFormat}
/**
 * Represents data associated with a specific field in the RC-210 system.
 *
 * @param fieldKey   The unique identifier for the field, combining a key and field name.
 * @param fieldValue The current value of the field.
 * @param candidate  An optional candidate value that can replace the current field value.
 *
 *                   This case class provides functionality to manage and update field data, allowing for a temporary
 *                   candidate value to be proposed and either accepted or replaced. The `value` property represents
 *                   the active value of the field, which defaults to the candidate if present, or the `fieldValue`
 *                   otherwise. The `display` property provides a string representation of the active value.
 */
case class FieldData(key:Key, fieldValue: FieldValue, candidate: Option[FieldValue] = None):
  /**
   * Retrieves the active value for the current field data.
   *
   * This value is determined based on the presence of a candidate value.
   * If a candidate value is defined, it is used as the active value.
   * Otherwise, the default field value (`fieldValue`) is used.
   */
  val value: FieldValue = candidate.getOrElse(fieldValue)
  val display: String = value.toString

  def acceptCandidate(): FieldData =
    copy(fieldValue = candidate.getOrElse(fieldValue), candidate = None)

  def setCandidate(newFieldValue: FieldValue): FieldData =
    if fieldValue == newFieldValue then
      copy(candidate = None)
    else
      copy(candidate = Option(newFieldValue))
  def rollBack: FieldData =
    copy(candidate = None)

object FieldData:
  implicit val ordering: Ordering[FieldData] =
    Ordering.by[FieldData, FieldKey](_.fieldKey)
    
  implicit val fmt: OFormat[FieldData] = Json.format[FieldData]


