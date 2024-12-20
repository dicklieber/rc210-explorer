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

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import jakarta.inject.{Inject, Singleton}
import net.wa9nnn.rc210
import net.wa9nnn.rc210.util.Configs
import net.wa9nnn.rc210.{Key, NamedKey, NamedKeySource}
import play.api.libs.json.Json

import java.nio.file.{Files, NoSuchFileException, Path}
import scala.collection.concurrent.TrieMap

@Singleton
class NamedKeyManager @Inject()(implicit config: Config) extends NamedKeySource with LazyLogging:
  private val keyNameMap = new TrieMap[Key, String]
  Key.setNamedSource(this)

  private val path: Path = Configs.path("vizRc210.namedDataFile")
  // load last saved data.
  try
    val str: String = Files.readString(path)
    val jsValue = Json.parse(str)
    val loadedKeys:Seq[NamedKey] = jsValue.as[Seq[NamedKey]]
    keyNameMap.addAll(loadedKeys.map(namedKey => namedKey.key -> namedKey.name))
  catch
    case e:NoSuchFileException =>
      logger.debug(s"No named keys file found at $path")
    case e: Exception =>
      logger.error(s"Error loading named keys from $path", e)
  
  private def save(): Unit =
    val sJson = Json.prettyPrint(Json.toJson(namedKeys))
    Files.writeString(path, sJson)
  
  /**
   * Adds or removes name keys.
   *
   * @param namedKey If the [[NamedKey.name]] is blank it is removed, otherwise updated.
   */
  def update(namedKey: Seq[NamedKey]): Unit =
    namedKey.foreach { namedKey =>
      val key = namedKey.key
      if namedKey.name.isBlank then
        keyNameMap.remove(key)
      else
        keyNameMap.put(key, namedKey.name)
    }
    save()

  def update(namedKey: NamedKey): Unit =
    update(Seq(namedKey))

  /**
   * Saves the named keys from the provided form data.
   *
   * @param formData Represents the submitted HTML form data containing key-value pairs.
   */
  def saveNamedKeys(formData: FormData):Unit=
    logger.error("handle named keys")
    

  override def nameForKey(key: Key): String = keyNameMap.getOrElse(key, "")

  override def namedKeys: Seq[NamedKey] =
    keyNameMap.map((key, name) => NamedKey(key, name)).toIndexedSeq.sorted

//  keyNameMap.addAll(dto.namedKeys.map(namedKey => namedKey.key -> namedKey.name))
object NamedKeyManager:
  val NoNamedKeySource: NamedKeySource = new NamedKeySource:
    override def nameForKey(key: Key) =
      throw new NotImplementedError() //todo

    override def namedKeys: Seq[NamedKey] =
      throw new NotImplementedError() //todo


