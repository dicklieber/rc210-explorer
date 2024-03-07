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

package net.wa9nnn.rc210.data.datastore

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210
import net.wa9nnn.rc210.*
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldValue}
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.Who.given
import net.wa9nnn.rc210.security.authentication.RcSession
import play.api.mvc.{AnyContent, Request}

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
 * This is the in-memory source of all RC-210 and NamedKey data.
 */
@Singleton
class DataStore @Inject()(persistence: DataStorePersistence, memoryFileLoader: MemoryFileLoader)
  extends NamedKeySource with LazyLogging:

  /**
   * This is all the data tht a user can edit that can be sent or received from an RC-210.
   */
  private implicit val keyFieldMap: TrieMap[FieldKey, FieldEntry] = new TrieMap[FieldKey, FieldEntry]()
  /**
   * This is the user-suplied metadata about a [[Key]].
   */
  private val keyNameMap = new TrieMap[Key, String]
  rc210.Key.setNamedSource(this) // so any Key can get it's user-supplied name.

  loadFromMemory()
  loadFromJson()

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
    keyFieldMap.values.toIndexedSeq.sorted

  def apply(dataTransferJson: DataTransferJson): Unit =
    throw new NotImplementedError() //todo

  def indexValues[T <: FieldValue](keyKind: KeyKind): Seq[T] =
    all.filter(_.fieldKey.key.keyKind == keyKind).map(_.value.asInstanceOf[T])

  def apply(fieldKey: FieldKey): FieldEntry =
    keyFieldMap.get(fieldKey) match
      case Some(fieldEntry) =>
        fieldEntry
      case None =>
        throw new Exception(s"No value for fieldKey: $fieldKey")

  //  def apply(keyKind: KeyKind): Seq[FieldEntry] =
  //    all.filter(_.fieldKeyStuff.key.keyKind == keyKind).sorted

  def apply(key: Key): Seq[FieldEntry] =
    all.filter(_.fieldKey.key == key).sorted

  def candidates: Seq[FieldEntry] =
    all.filter(_.candidate.nonEmpty).sorted

  def triggerNodes(macroKey: Key): Seq[FieldEntry] =
    assert(macroKey.keyKind == KeyKind.Macro, "Must have a MacroKey!")
    (for {
      fieldEntry <- keyFieldMap.values
      fieldValue: FieldValue = fieldEntry.value[FieldValue]
      if fieldValue.canRunMacro(macroKey)
    } yield {
      fieldEntry
    }).toIndexedSeq

  def update(candidateAndNames: CandidateAndNames)(using rcSession: RcSession): Unit =
    candidateAndNames.candidates.foreach { uc =>
      val fieldKey = uc.fieldKey
      val current: FieldEntry = keyFieldMap(fieldKey)
      keyFieldMap.put(fieldKey, uc.candidate match
        case value: String =>
          current.setCandidate(value)
        case value: ComplexFieldValue =>
          current.setCandidate(value))
    }

    candidateAndNames.namedKeys foreach { nammedKey =>
      val key = nammedKey.key
      if (nammedKey.name.isBlank)
        keyNameMap.remove(key)
      else
        keyNameMap.put(key, nammedKey.name)
    }

    save(rcSession)

  def acceptCandidate(fieldKey: FieldKey)(using rcSession: RcSession): Unit =
    keyFieldMap.put(fieldKey, keyFieldMap(fieldKey).acceptCandidate())
    save(rcSession)

  def rollback(): Unit =
    keyFieldMap.values.foreach { fieldEntry =>
      val updated = fieldEntry.copy(candidate = None)
      keyFieldMap.put(fieldEntry.fieldKey, updated)
    }

  def rollback(fieldKey: FieldKey): Unit =
    val fieldEntry = apply(fieldKey)
    val updated = fieldEntry.copy(candidate = None)
    keyFieldMap.put(fieldEntry.fieldKey, updated)

  def reload(): Unit =
    loadFromMemory()

  private def save(session: RcSession): Unit =
    val dto: DataTransferJson = toJson.copy(who = Some(session.user.who))
    persistence.save(dto)

  def loadFromJson(): Unit = {
    // update values from datastore.json
    try
      persistence.load().foreach { dto => {
        dto.values.foreach { fieldEntryJson =>
          val fieldKey = fieldEntryJson.fieldKey
          keyFieldMap.get(fieldKey).foreach { fieldEntry =>
            val newFieldValue: FieldValue = fieldEntry.fieldDefinition.parse(fieldEntryJson.fieldValue)
            val newCandidate: Option[FieldValue] = fieldEntryJson.candidate.map(fieldEntry.fieldDefinition.parse)

            val updated = fieldEntry.copy(fieldValue = newFieldValue, candidate = newCandidate)
            keyFieldMap.put(fieldKey, updated)
          }
        }
        // update namedKeys from datastore.json
        keyNameMap.clear()
        keyNameMap.addAll(dto.namedKeys.map(namedKey => namedKey.key -> namedKey.name))
      }
      }
    catch
      case e: Exception =>
        logger.error("Loading", e)
  }

  private def loadFromMemory(): Unit =
    memoryFileLoader.load match
      case Failure(exception) =>
        logger.error("Loading DataStore from Download Memory image.", exception)
      case Success(fieldEntries: Seq[FieldEntry]) =>
        fieldEntries.foreach { fe =>
          keyFieldMap.put(fe.fieldKey, fe)
        }

  def toJson: DataTransferJson =
    DataTransferJson(values =
      keyFieldMap.values.map(FieldEntryJson(_)).toSeq,
      namedKeys = keyNameMap.map { case (key, name) => NamedKey(key, name) }.toSeq)

  override def nameForKey(key: Key): String = keyNameMap.getOrElse(key, "")

  override def namedKeys: Seq[NamedKey] =
    keyNameMap.map((key, name) => NamedKey(key, name)).toIndexedSeq.sorted

  def triggers: Seq[FieldEntry] =
    (for {
      fieldEntry <- keyFieldMap.values
      value: FieldValue = fieldEntry.value
      if value.canRunAny
    } yield {
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

    val entries: Seq[FieldEntry] = apply(search)
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


