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

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.KeyIndicator.{from, iValue}
import net.wa9nnn.rc210.{Key, KeyIndicator, NamedKey}
import play.api.mvc.{AnyContent, Request}

import scala.collection.immutable

/**
 * Represents form data extracted from an HTML form as a map of field names to their corresponding values.
 *
 * @param formData This is what Play gives us from an HTML form post. 
 */
class FormData(formData: Map[String, Seq[String]]) extends LazyLogging:
  logger.whenTraceEnabled {
    formData.foreach(kv => logger.trace(s"FormData: ${kv._1} -> ${kv._2.mkString(", ")}"))
  }
  private val all =
    for
      (id, values) <- formData
      if KeyIndicator.hasIndicator(id) // Just ones that are actually [[Keys]]s
    yield
      val key = Key.fromId(id)
      KeyAndValues(key, values)

  val data: Map[Key, KeyAndValues] = all.filter(_._1.indicator == KeyIndicator.iValue)
    .map { kv => kv._1 -> kv }.toMap
  val maybeKey: Option[Key] = all.find(_._1.indicator == KeyIndicator.iKey)
    .map(_.key.withIndicator(iValue))
  val namedKeys: Seq[NamedKey] = all.filter(_._1.indicator == KeyIndicator.iName)
    .map(NamedKey(_)).toSeq
  /**
   *
   * @return what can vew passed to play.api.data.Form#bind
   */
  lazy val bindable: Map[String, String] =
    for
      (id, values) <- formData
      if !KeyIndicator.hasIndicator(id) // Just ones that are actually [[Keys]]s
    yield
      id -> values.head
    
  def valueOpt(qualifier: String): Option[String] =
    val x: Option[KeyAndValues] = data.values.find(_.key.qualifier.contains(qualifier))
    val y: Option[String] = x.map(_.head)
    y

  /**
   * Get a value for a name in the HTML form data.
   *
   * @param qualifier
   * @return the 1st value or None
   */
  def value(qualifier: String): String =
    valueOpt(qualifier).getOrElse("")


object FormData:
  def apply()(using request: Request[AnyContent]): FormData =
    new FormData(request.body.asFormUrlEncoded.get)

case class KeyAndValues(key: Key, values: Seq[String]):
  /**
   * Provides the first element from the `values` collection that matches a specified `PartialFunction`.
   * If no matches are found, returns an empty string.
   */
  val head: String = values.iterator.collectFirst(pf => pf).getOrElse("")