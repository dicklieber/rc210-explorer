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

import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.{UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.ComplexFieldValue
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.key.KeyFactory.Key
import play.api.mvc.AnyContentAsFormUrlEncoded

object FormParser {
  /**
   * Parses [[ComplexFieldValue]]s from <form> data.
   *
   * @param f       (K,  Map[String, String]) )      function to instantiate a [[ComplexFieldValue[K]]] from a K  and map of named form values for the key.
   * @param content whose boduy contains the HTML form data.
   * @tparam K Key type.
   * @return data to send to the [[net.wa9nnn.rc210.data.datastore.DataStore]].
   */
  def apply[K <: Key](content: AnyContentAsFormUrlEncoded, f: (K, Map[String, String]) => ComplexFieldValue[K]): UpdateData = {
    val namedKeyBuilder = Seq.newBuilder[NamedKey]
    val candidates: Seq[UpdateCandidate] = content.data
      .map { t => FieldKey.fromParam(t._1) -> t._2.head } // convert form inpout name to FieldKey
      .groupBy(_._1.key)
      // build map of name to values for each Key
      .map { case (key: Key, values: Map[FieldKey, String]) =>
        val valueMap: Map[String, String] = values.map { case (fk, value) =>
          fk.fieldName -> value
        }
        valueMap.get("name").foreach {
          namedKeyBuilder += NamedKey(key, _)
        }
        val complexValue = f(key.asInstanceOf[K], valueMap)
        UpdateCandidate(complexValue.fieldKey, Right(complexValue))
      }.toSeq

    UpdateData(candidates, namedKeyBuilder.result())
  }

  /**
   * Parses [[net.wa9nnn.rc210.data.field.SimpleFieldValue]]s from <form> data
   *
   * @param content whose boduy contains the HTML form data.
   * @return data to send to the [[net.wa9nnn.rc210.data.datastore.DataStore]].
   */
  def apply(content: AnyContentAsFormUrlEncoded): UpdateData = {


    val namedKeyBuilder = Seq.newBuilder[NamedKey]
    val candidateBuilder = Seq.newBuilder[UpdateCandidate]
    content
      .data
      .filter(_._1 != "save")
      .map { t => FieldKey.fromParam(t._1) -> t._2.head } // convert form <input> name to FieldKey and on;y get 1st string for each <form> item.
      .foreach { case (fieldKey: FieldKey, value: String) =>
        fieldKey match {
          case FieldKey("name", _) =>
            namedKeyBuilder += NamedKey(fieldKey.key, value)
          case _ =>
            candidateBuilder += UpdateCandidate(fieldKey, Left(value)) // this string will get parsed within the FieldValues in the [[DataStore]].
        }
      }
    UpdateData(candidateBuilder.result(), namedKeyBuilder.result())
  }
}
