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
import jakarta.inject.{Inject, Named, Singleton}
import net.wa9nnn.rc210
import net.wa9nnn.rc210.KeyIndicator.iName
import net.wa9nnn.rc210.{Key, KeyIndicator, NamedKey}
import play.api.libs.json.Json
import play.twirl.api.Html

import java.nio.file.{Files, NoSuchFileException, Path, Paths}
import scala.collection.concurrent.TrieMap
import scala.collection.immutable
import scala.xml.Elem

/**
 * Manages named keys by maintaining a mapping between keys and their corresponding names.
 *
 * This class allows for loading, updating, and saving named keys from a specified file.
 * It extends the `NamedKeySource` trait to provide key-to-name resolution functionality
 * and makes use of lazy logging for reporting errors and informational messages.
 *
 * @param namedDataFile path of the file used to store named keys.
 */
class NamedKeyManager @Inject()(@Named("namedDataFile") namedDataFile: String)
  extends NamedKeySource with LazyLogging:
  def this(path: Path) = this(path.toFile.toString)

  NamedKeyManager._namedKeySource = this
  private val keyNameMap = new TrieMap[Key, String]

  val path: Path = Paths.get(namedDataFile)
  // load last saved data.
  try
    val str: String = Files.readString(path)
    val jsValue = Json.parse(str)
    val loadedKeys: Seq[NamedKey] = jsValue.as[Seq[NamedKey]]
    keyNameMap.addAll(loadedKeys.map(namedKey => namedKey.key -> namedKey.name))
  catch
    case _: NoSuchFileException =>
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
      val key = namedKey.key.withIndicator(KeyIndicator.iValue)
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
  def saveNamedKeys(formData: FormData): Unit =
    update(formData.namedKeys)

  override def nameForKey(key: Key): String = keyNameMap.getOrElse(key, "")

  def namedKeys: Seq[NamedKey] =
    keyNameMap.map((key, name) => NamedKey(key, name)).toIndexedSeq.sorted

object NamedKeyManager extends NamedKeySource:
  var _namedKeySource: NamedKeySource = new NamedKeySource:
    override def nameForKey(key: Key) = "???"

  def nameForKey(key: Key): String =
    _namedKeySource.nameForKey(key)

  /**
   * Use in twirl template e.g. @NameEdit.html(key)
   * Shows the number followed by an <input> field with the name.
   *
   * @param key
   * @return
   */
  def keyAndNameEdit(key: Key): Html =
    val namedKey: NamedKey = key.namedKey
    val keyName = key.name
    val parmName =  namedKey.key.withIndicator(iName).id

    val r: Elem =
      <div>
        {namedKey.key.toString}
        : Name:
        <input name={parmName} value={keyName}></input>
      </div>
    Html(r.toString)

trait NamedKeySource:
  /**
   *
   * @return empty string of key does not have a name.
   */
  def nameForKey(key: Key): String

//object NameEdit:
//  def cell(key: Key): Cell =
//    Cell.rawHtml(html(key).body)
//
