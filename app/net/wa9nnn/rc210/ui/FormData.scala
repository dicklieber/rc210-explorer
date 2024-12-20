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

package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.Key.keyName
import play.api.mvc.{AnyContent, Request}

/**
 * Represents form data extracted from an HTML form as a map of field names to their corresponding values.
 *
 * @param map A map where each key is a field name, and each value is a sequence of strings representing the field's values.
 */
class FormData(val map: Map[String, Seq[String]]):
  /**
   * Get a value for a name in the HTML form data.
   *
   * @param fieldName from the name="xyz" in an HTML form control.
   * @return the 1st value or None
   */
  def apply(fieldName: String): Option[String] =
    map.get(fieldName).flatMap(_.headOption)

  def allValues(fieldName: String): Seq[String] =
    map.getOrElse(fieldName, Seq.empty)

  def key: Key =
    apply(Key.keyName)
      .map(Key(_))
      .getOrElse(throw new IllegalArgumentException(s"No key found in form data!"))

object FormData:
  def apply()(using request: Request[AnyContent]): FormData =
    new FormData(request.body.asFormUrlEncoded.get)


