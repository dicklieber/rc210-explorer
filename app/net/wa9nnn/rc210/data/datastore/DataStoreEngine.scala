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

import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.data.macros.MacroNode

import scala.collection.concurrent.TrieMap

/**
 * The `DataStoreEngine` class is an implementation of the `DataStoreApi` trait. It acts as an in-memory data store
 * for managing `FieldEntry` objects identified by their `FieldKey`. 
 * The class provides methods to load, retrieve, update, and manipulate these stored field entries. 
 * It supports operations like accepting candidate values, rolling back changes, and filtering entries based on various criteria.
 */
class DataStoreEngine extends DataStoreApi:
  private implicit val keyFieldMap: TrieMap[FieldKey, FieldEntry] = new TrieMap[FieldKey, FieldEntry]()

  /**
   * 
   * @param entries as returned by [[fieldDatas]].
   */
  def loadEntries(entries:Iterable[FieldData]):Unit =
    entries.foreach{fieldData =>
      keyFieldMap.get(fieldData.fieldKey) match
        case Some(fieldEntry) => 
          fieldEntry.setFieldData(fieldData)
        case None => 
          logger.error(s"No entry for fieldKey: ${fieldData.fieldKey}")
    }
  /**
   * Retrieves all `FieldEntry` objects stored in the `DataStoreEngine`, sorted by their keys.
   *
   * @return a sequence of sorted `FieldEntry` instances.
   */
  def fieldDatas: Seq[FieldData] =
    keyFieldMap.values.map(_.fieldData).toIndexedSeq.sorted

  def values[T <: ComplexFieldValue](keyKind: KeyKind): Seq[T] =
    apply(keyKind).map(_.value.asInstanceOf[T])

  def editValue[T <: FieldValue](fieldKey: FieldKey): T =
    val maybeFieldEntry: Option[FieldEntry] = all.find(_.fieldKey == fieldKey)
    maybeFieldEntry match
      case Some(fieldEntry: FieldEntry) =>
        fieldEntry.value.asInstanceOf[T]
      case None =>
        throw new IllegalArgumentException(s"No editValue for fieldKeyStuff: $fieldKey")

  def apply(keyKind: KeyKind): Seq[FieldEntry] =
    all.filter(_.fieldKey.key.keyKind == keyKind)

  def all: Seq[FieldEntry] =
    keyFieldMap.values.toIndexedSeq//.sorted
  
//  def indexValues[T <: FieldValue](keyKind: KeyKind): Seq[T] =
//    all.filter(_.fieldKey.key.keyKind == keyKind).map(_.value.asInstanceOf[T])

  def fieldEntry(fieldKey: FieldKey): FieldEntry =
    keyFieldMap.get(fieldKey) match
      case Some(fieldEntry) =>
        fieldEntry
      case None =>
        throw new Exception(s"No value for fieldKey: $fieldKey")

  //  def get(keyKind: KeyKind): Seq[FieldEntry] =
  //    all.filter(_.fieldKeyStuff.key.keyKind == keyKind).sorted

  def fieldEntry(key: Key): Seq[FieldEntry] =
    all.filter(_.fieldKey.key == key)//.sorted

  def candidates: Seq[FieldEntry] =
    all.filter(_.hasCandidate)

  def triggerNodes(macroKey: Key): Seq[FieldEntry] =
    assert(macroKey.keyKind == KeyKind.Macro, "Must have a MacroKey!")
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
   * Updates the candidates of field entries in the `keyFieldMap` based on the provided `CandidateAndNames`.
   *
   * @param candidateAndNames contains a sequence of `UpdateCandidate` objects that define the `fieldKey` and 
   *                          the new candidate value to be updated, as well as an optional sequence of `NamedKey`.
   */
  def update(candidateAndNames: CandidateAndNames): Unit =
    candidateAndNames.candidates.foreach { updateCandidate =>
      val fieldKey = updateCandidate.fieldKey
      val fieldEntry: FieldEntry = keyFieldMap(fieldKey)
      updateCandidate.candidate match
        case str: String =>
          fieldEntry.setCandidate(str) //todo
        case value: ComplexFieldValue =>
          fieldEntry.setCandidate(value)
    }

  /**
   * Marks the candidate value associated with the specified `FieldKey` as accepted
   * and updates the field's value in the `keyFieldMap`.
   *
   * @param fieldKey   the key identifying the field whose candidate value is to be accepted.
   */
  def acceptCandidate(fieldKey: FieldKey): Unit =
    keyFieldMap(fieldKey).acceptCandidate()

  def clearCandidates(): Unit =
    keyFieldMap.values.foreach { fieldEntry =>
      fieldEntry.clearCandidate()
    }

  def clearCandidate(fieldKey: FieldKey): Unit =
    fieldEntry(fieldKey).clearCandidate()

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

  def flowData(search: Key): Option[FlowData] =
    def buildFlowData(MacroEntry: FieldEntry): FlowData =
      assert(MacroEntry.fieldKey.key.keyKind == KeyKind.Macro, s"Must be an Macro entry!  But got: $MacroEntry")

      // find triggers
      val Macro: MacroNode = MacroEntry.value
      val key = Macro.key

      val triggers: Seq[FieldEntry] = triggerNodes(key)

      new FlowData(MacroEntry, triggers, search)

    val entries: Seq[FieldEntry] = fieldEntry(search)
    assert(entries.length == 1, s"Should only be one entry for a complex key, but got $entries")
    entries.headOption.map { fieldEntry =>
      fieldEntry.fieldKey.key.keyKind match

        case KeyKind.Macro =>
          buildFlowData(fieldEntry)
        case other =>
          val entry: FieldValue = fieldEntry.value
          val key: Key = entry.runableMacros.head
          flowData(key).get
    }
