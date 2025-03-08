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

package net.wa9nnn.rc210.data.datastore

import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.libs.Files.logger

import scala.collection.concurrent.TrieMap

/**
 * The `DataStoreEngine` class is an implementation of the `DataStoreApi` trait. It acts as an in-memory data store
 * for managing `FieldEntry` objects identified by their `Key`. 
 * The class provides methods to load, retrieve, update, and manipulate these stored field entries. 
 * It supports operations like accepting candidate values, rolling back changes, and filtering entries based on various criteria.
 */
class DataStoreEngine extends DataStoreApi:
  private implicit val keyFieldMap: TrieMap[Key, FieldEntry] = new TrieMap[Key, FieldEntry]()

  /**
   *
   * @param entries as returned by [[entries]].
   */
  def loadDefinitions(entries: Seq[FieldEntry]): Unit =
    entries.foreach { fieldEntry =>
      keyFieldMap.put(fieldEntry.key, fieldEntry)
    }

  def set(fieldDatas: Seq[FieldData]): Unit =
    fieldDatas.foreach { fieldData =>
      keyFieldMap.get(fieldData.key).foreach(fieldEntry =>
        fieldEntry.set(fieldData)
      )
    }

  /**
   * Retrieves all `FieldEntry` objects stored in the `DataStoreEngine`, sorted by their keys.
   *
   * @return a sequence of sorted `FieldEntry` instances.
   */
  def entries: Seq[FieldEntry] =
    keyFieldMap.values.toIndexedSeq.sorted

  def values[T <: FieldValue](keyKind: KeyMetadata): Seq[T] =
    apply(keyKind).map(_.value.asInstanceOf[T])

  def editValue[T <: FieldValue](Key: Key): T =
    val maybeFieldEntry: Option[FieldEntry] = all.find(_.key == Key)
    maybeFieldEntry match
      case Some(fieldEntry: FieldEntry) =>
        fieldEntry.value.asInstanceOf[T]
      case None =>
        throw new IllegalArgumentException(s"No editValue for KeyStuff: $Key")

  def apply(keyMetadata: KeyMetadata): Seq[FieldEntry] =
    all.filter(_.key.keyMetadata == keyMetadata)

  def all: Seq[FieldEntry] =
    keyFieldMap.values.toIndexedSeq.sorted

  //  def indexValues[T <: FieldValue](keyKind: KeyKind): Seq[T] =
  //    all.filter(_.Key.key.keyKind == keyKind).map(_.value.asInstanceOf[T])

  def getFieldEntry(Key: Key): FieldEntry =
    keyFieldMap.get(Key) match
      case Some(fieldEntry) =>
        fieldEntry
      case None =>
        throw new Exception(s"No value for Key: $Key")

  def fieldDefinition(Key: Key): Option[FieldDef[?]] =
    keyFieldMap.get(Key).map(_.fieldDefinition)

  //  def get(keyKind: KeyKind): Seq[FieldEntry] =
  //    all.filter(_.KeyStuff.key.keyKind == keyKind).sorted

  def fieldEntry(keyMetadata: KeyMetadata): Seq[FieldEntry] =
    all.filter(_.key.keyMetadata == keyMetadata).sorted

  def candidates: Seq[FieldEntry] =
    keyFieldMap.values.filter(_.fieldData.candidate.nonEmpty).toSeq

  def triggerNodes(macroKey: Key): Seq[FieldEntry] =
    assert(macroKey.keyMetadata == KeyMetadata.Macro, "Must have a MacroKey!")
    (for
    {
      fieldEntry <- keyFieldMap.values
      fieldValue: FieldValue = fieldEntry.value[FieldValue]
      if fieldValue.canRunMacro(macroKey)
    } yield
    {
      fieldEntry
    }).toIndexedSeq

  /**
   * @param candidates a sequence of `UpdateCandidate` instances containing the keys and values
   *                   used to update the corresponding fields in the `keyFieldMap`.
   */

  def update(candidates: Seq[UpdateCandidate]): Unit =
    candidates.foreach { updateCandidate =>
      val key = updateCandidate.key
      val fieldEntry = getFieldEntry(key)

      updateCandidate.candidate match
        case str: String =>
          fieldEntry.fieldDefinition match
            case defSimple: FieldDefSimple[?] =>
              val candidateValue = defSimple.fromString(str)
              fieldEntry.set(candidateValue)
            case _ =>
              logger.error(s"""No simple definition for key: "$key"!""")

        case candidateValue: FieldValue =>
          fieldEntry.set(candidateValue)
    }

  /**
   * Marks the candidate value associated with the specified `Key` as accepted
   * and updates the field's value in the `keyFieldMap`.
   *
   * @param Key   the key identifying the field whose candidate value is to be accepted.
   */
  def acceptCandidate(Key: Key): Unit =
    keyFieldMap(Key).acceptCandidate()

  def rollback(): Unit =
    keyFieldMap.values.foreach { fieldEntry =>
      fieldEntry.rollBack
    }

  def rollback(Key: Key): Unit =
    val aFieldEntry = getFieldEntry(Key)
    aFieldEntry.rollBack

  def triggers: Seq[FieldEntry] =
    (for
    {
      fieldEntry <- keyFieldMap.values
      value: FieldValue = fieldEntry.value
      if value.canRunAny
    } yield
    {
      fieldEntry
    }).toIndexedSeq

  def flowData(searchKey: Key): Option[FlowData] =
    def buildFlowData(MacroEntry: FieldEntry): FlowData =
      assert(MacroEntry.key.keyMetadata == KeyMetadata.Macro, s"Must be an Macro entry!  But got: $MacroEntry")
      val triggers: Seq[FieldEntry] = triggerNodes(searchKey)
      new FlowData(MacroEntry, triggers, searchKey)

    val entries: Seq[FieldEntry] = fieldEntry(searchKey.keyMetadata)
    assert(entries.length == 1, s"Should only be one entry for a complex key, but got $entries")
    entries.headOption.map { fieldEntry =>
      fieldEntry.key.keyMetadata match

        case KeyMetadata.Macro =>
          buildFlowData(fieldEntry)
        case other =>
          val entry: FieldValue = fieldEntry.value
          val key: Key = entry.runableMacros.head
          flowData(key).get
    }
