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

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.google.inject.Provides
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldValue}
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.named.{NamedKey, NamedSource}
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.model.TriggerNode
import net.wa9nnn.rc210.security.authentication.User
import net.wa9nnn.rc210.ui.CandidateAndNames
import play.api.libs.concurrent.ActorModule
import play.api.libs.json

import scala.collection.concurrent.TrieMap

object DataStoreActor extends ActorModule  with LazyLogging {
  sealed trait DataStoreMessage

  type Message = DataStoreMessage

  case class ReplaceEntries(entries: Seq[FieldEntry]) extends DataStoreMessage

  case class All(replyTo: ActorRef[Seq[FieldEntry]]) extends DataStoreMessage

  case class AllForKey(key: Key, replyTo: ActorRef[Seq[FieldEntry]]) extends DataStoreMessage

  case class AllForKeyKind(keyKind: KeyKind, replyTo: ActorRef[Seq[FieldEntry]]) extends DataStoreMessage

  case class ForFieldKey(fieldKey: FieldKey, replyTo: ActorRef[Option[FieldEntry]]) extends DataStoreMessage


  case class Json(replyTo: ActorRef[String]) extends DataStoreMessage

  case class IngestJson(sJson: String, replyTo: ActorRef[String]) extends DataStoreMessage

  case class Candidates(replyTo: ActorRef[Seq[FieldEntry]]) extends DataStoreMessage

  case class Triggers(replyTo: ActorRef[Seq[MacroWithTriggers]]) extends DataStoreMessage

  case class AcceptCandidate(fieldKey: FieldKey, user: User) extends DataStoreMessage


  case class UpdateData(candidates: Seq[UpdateCandidate], namedKeys: Seq[NamedKey] = Seq.empty, user: User, replyTo: ActorRef[String]) extends DataStoreMessage

  object UpdateData {
    def apply(candidateAndNames: CandidateAndNames, user: User, replyTo: ActorRef[String]): UpdateData = {
      new UpdateData(candidateAndNames.candidates, candidateAndNames.namedKeys, user, replyTo)
    }
  }


  case class UpdateFields(fieldEntries: Seq[FieldEntry], names: Seq[NamedKey] = Seq.empty, user: User) extends DataStoreMessage

//  Key.setNamedSource(this)

  @Provides def apply(persistence: DataStorePersistence, memoryFileLoader: MemoryFileLoader): Behavior[DataStoreMessage] = {
    Behaviors.setup[DataStoreMessage] { actorContext =>

      def loadFromRcMemory(): TrieMap[FieldKey, FieldEntry] = {
        val map = new TrieMap[FieldKey, FieldEntry]
        memoryFileLoader.load.foreach { fieldEntry =>
          map.put(fieldEntry.fieldKey, fieldEntry)
        }
        map
      }

      var valuesMap: TrieMap[FieldKey, FieldEntry] = loadFromRcMemory()
      var keyNamesMap = new TrieMap[Key, String]

      def loadFromJson(): Unit = {
        // update values from datastore.json
        persistence.load().foreach { dto =>
          ingest(dto)
        }
      }

      def ingest(dto: DataTransferJson): Unit = {
        dto.values.foreach { fieldEntryJson: FieldEntryJson =>
          val fieldKey = fieldEntryJson.fieldKey
          valuesMap.get(fieldKey).foreach { fieldEntry =>
            val newFieldValue: FieldValue = fieldEntry.fieldDefinition.parse(fieldEntryJson.fieldValue)
            val newCandidate: Option[FieldValue] = fieldEntryJson.candidate.map(fieldEntry.fieldDefinition.parse)

            val updated = fieldEntry.copy(fieldValue = newFieldValue, candidate = newCandidate)
            valuesMap.put(fieldKey, updated)
          }
        }
        // update namedKeys from datastore.json
        keyNamesMap.clear()
        keyNamesMap.addAll(dto.namedKeys.map(namedkey => namedkey.key -> namedkey.name))

      }


      memoryFileLoader.load
      loadFromJson()

      def all: Seq[FieldEntry] = valuesMap.values.toSeq

      def save(user: User): Unit = {
        val dto = DataTransferJson(values = valuesMap.values.map(FieldEntryJson(_)).toSeq,
          namedKeys = keyNamesMap.map { case (key, name) => NamedKey(key, name) }.toSeq,
          who = Option(user.who))
        persistence.save(dto)
      }

      Behaviors.receiveMessage { message =>
        message match {
          case ReplaceEntries(entries: Seq[FieldEntry]) =>

          case All(replyTo: ActorRef[Seq[FieldEntry]]) =>
            replyTo ! all.sorted
          case AllForKey(key: Key, replyTo: ActorRef[Seq[FieldEntry]]) =>
            replyTo ! all
              .filter(_.fieldKey.key == key)
              .sortBy(_.fieldKey.fieldName)
          case AllForKeyKind(keyKind: KeyKind, replyTo: ActorRef[Seq[FieldEntry]]) =>
            replyTo ! all.filter(_.fieldKey.key.kind == keyKind).sortBy(_.fieldKey)
          case ForFieldKey(fieldKey: FieldKey, replyTo: ActorRef[Option[FieldEntry]]) =>
            replyTo ! all.find(_.fieldKey == fieldKey)
          case Candidates(replyTo: ActorRef[Seq[FieldEntry]]) =>
            replyTo ! all.filter(_.candidate.nonEmpty)
          case Triggers(replyTo: ActorRef[Seq[MacroWithTriggers]]) =>
            val triggerNodes: Seq[TriggerNode] = all.filter { fe => fe.isInstanceOf[TriggerNode] }.map(_.asInstanceOf[TriggerNode])
            replyTo !
              (for {
                fieldEntry <- all.filter(_.fieldKey.key.kind == KeyKind.macroKey).sorted
                macroNode: MacroNode = fieldEntry.fieldValue.asInstanceOf[MacroNode]
              } yield {
                val triggers: Seq[TriggerNode] = triggerNodes.filter(_.canRunMacro(macroNode.key))
                MacroWithTriggers(macroNode = macroNode, triggers = triggers)
              })

          case IngestJson(sJson, replyTo) =>
            ingest(json.Json.parse(sJson).as[DataTransferJson])
            replyTo ! "Done"

          case Json(replyTo) =>

            val dto = DataTransferJson(values = valuesMap.values.map(FieldEntryJson(_)).toSeq,
              namedKeys = keyNamesMap.map { case (key, name) => NamedKey(key, name) }.toSeq)
            replyTo ! persistence.toJson(dto)

          case AcceptCandidate(fieldKey: FieldKey, user: User) =>
            valuesMap.put(fieldKey, valuesMap(fieldKey).acceptCandidate())
            save(user)
          case ud: UpdateData =>
            ud.candidates.foreach { candidate: UpdateCandidate =>
              val fieldKey = candidate.fieldKey

              val currentEntry = valuesMap(candidate.fieldKey)
              val newEntry: FieldEntry = candidate.candidate match {
                case Left(formValue: String) =>
                  currentEntry.setCandidate(formValue)

                case Right(value: ComplexFieldValue[_]) =>
                  currentEntry.setCandidate(value)
                case x =>
                  logger.error(s"Neither right nor left.  $x")
                  throw new NotImplementedError() //todo
              }
              valuesMap.put(fieldKey, newEntry)
            }
            ud.namedKeys.foreach { namedKey =>
              val key = namedKey.key
              val name = namedKey.name
              if (name.isBlank)
                keyNamesMap.remove(key)
              else
                keyNamesMap.put(key, name)
            }
            save(ud.user)
            ud.replyTo ! "Done"
          case UpdateFields(fieldEntries: Seq[FieldEntry], names: Seq[NamedKey], user: User)
          =>
            fieldEntries.foreach { fieldEntry =>
              valuesMap.put(fieldEntry.fieldKey, fieldEntry)
            }
            save(user)

        }
        Behaviors.same
      }
    }
  }


//  override def nameForKey(key: Key): String =
//    throw new NotImplementedError() //todo non message entry into Actor!
}
