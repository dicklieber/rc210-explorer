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
import jakarta.inject.Singleton
import net.wa9nnn.rc210.{Key, NamedKey, NamedKeySource}
import net.wa9nnn.rc210
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.ui.NamedKeyManager.inTest

import scala.collection.concurrent.TrieMap

@Singleton
class NamedKeyManager extends NamedKeySource with LazyLogging:
  private val keyNameMap = new TrieMap[Key, String]
  try
    rc210.Key.setNamedSource(this)
  catch
    case e: IllegalStateException =>
      if (!inTest)
        logger.error("setNamedSource", e)

  // so any Key can get it's user-supplied name.

  /**
   * @param namedKeys These come from [[DataTransferJson]] managed by the [[DataStore]].
   */
  def load(namedKeys: Seq[NamedKey]): Unit =
    keyNameMap.clear()
    keyNameMap.addAll(namedKeys.map(namedKey => namedKey.key -> namedKey.name))

  def save: Seq[NamedKey] =
    // update namedKeys from datastore.json
    keyNameMap.map { case (key, name) =>
      NamedKey(key, name)
    }.toSeq

  def update(namedKey: Seq[NamedKey]): Unit =
    namedKey.foreach { namedKey =>
      val key = namedKey.key
      if namedKey.name.isBlank then
        keyNameMap.remove(key)
      else
        keyNameMap.put(key, namedKey.name)
    }

  override def nameForKey(key: Key): String = keyNameMap.getOrElse(key, "")

  override def namedKeys: Seq[NamedKey] =
    keyNameMap.map((key, name) => NamedKey(key, name)).toIndexedSeq.sorted

//  keyNameMap.addAll(dto.namedKeys.map(namedKey => namedKey.key -> namedKey.name))
object NamedKeyManager:
  var inTest: Boolean = false


