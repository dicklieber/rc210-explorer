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

import net.wa9nnn.rc210.{FieldKey, Key}
import net.wa9nnn.rc210.data.datastore.{CandidateAndNames, UpdateCandidate}
import net.wa9nnn.rc210.data.field.ComplexFieldValue
import net.wa9nnn.rc210.NamedKey
import play.api.mvc.{AnyContent, AnyContentAsFormUrlEncoded, Request}

import scala.collection.immutable

/**
 * Helpers that extract [[NamedKey]]s from a form request.
 */
object ProcessResult {
  def apply(fieldValue: ComplexFieldValue)(using request: Request[AnyContent]): CandidateAndNames =
    val data: Map[String, String] = request
      .body
      .asFormUrlEncoded
      .get
      .map(t => t._1 -> t._2.head)
    val sKey: String = data.getOrElse("key", throw new IllegalArgumentException("No key in form data!"))
    val key = Key(sKey)

    val namedKeys: Option[NamedKey] = data.get("name").map(name => NamedKey(key, name))

    val updateCandidate = UpdateCandidate(fieldValue.fieldKey, fieldValue)

    CandidateAndNames(updateCandidate, namedKeys)

  def apply(candidateAndNames: CandidateAndNames)(using request: Request[AnyContent]): CandidateAndNames =
    val data: Map[FieldKey, String] = request
      .body
      .asFormUrlEncoded
      .get
      .map(t => FieldKey(t._1) -> t._2.head)
    val named: Map[FieldKey, String] = data.filter(_._1.fieldName == "name")
    val namedKeys = named.map { (fieldKey, name) =>
      NamedKey(fieldKey.key, name)
    }

    candidateAndNames.copy(namedKeys = namedKeys.toSeq)

}
