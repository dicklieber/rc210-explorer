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

import net.wa9nnn.rc210.data.TriggerNode
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey, FieldValue}
import net.wa9nnn.rc210.data.named.{NamedKey, NamedKeySource}
import net.wa9nnn.rc210.security.authentication.{RcSession, User}

import scala.collection.concurrent.TrieMap
import scala.util.Try

/**
 * The logic behind the [[DataStoreActor]].
 * This is the in-memory source of all RC-210 and NamedKey data.
 */
class DataStoreLogic(persistence: DataStorePersistence) extends NamedKeySource {
  private implicit val keyFieldMap: TrieMap[FieldKey, FieldEntry] = new TrieMap[FieldKey, FieldEntry]()
  private val keyNameMap = new TrieMap[Key, String]
  Key.setNamedSource(this) // so any Key can get it's user-supplied name.


  def apply[R](askRequest: DataStoreRequest): DataStoreReply = {
    DataStoreReply(Try(askRequest.process))
  }
  //
  //
  ////        def all: Seq[FieldEntry] = data.values.toSeq
  //
  //        def entries(keyKind: KeyKind): Seq[FieldEntry] =
  //          all.filter(_.fieldKey.key.keyKind == keyKind).sortBy(_.fieldKey)
  //
  //        def entries(key: Key): Seq[FieldEntry] =
  //          all.filter(_.fieldKey.key == key).sortBy(_.fieldKey.fieldName)
  //
  //        def withCandidate: Seq[FieldEntry] = all.filter(_.candidate.nonEmpty).sortBy(_.fieldKey.fieldName)
  //
  //        def triggerNodes: Seq[TriggerNode] =
  //          all.flatMap { fieldEntry =>
  //            fieldEntry match
  //              case tn: TriggerNode =>
  //                Option.when(tn.nodeEnabled)(tn)
  //              case _ =>
  //                Seq.empty
  //          }
  //
  //        def acceptCandidate(fieldKey: FieldKey, user: User) =
  //          data.put(fieldKey, data(fieldKey).acceptCandidate())
  //          save(user)
  //

  def save(session: RcSession): Unit =
    val dto = DataTransferJson(values = keyFieldMap.values.map(FieldEntryJson(_)).toSeq,
      namedKeys = keyNameMap.map { case (key, name) => NamedKey(key, name) }.toIndexedSeq,
      who = Option(session.user.who))
    persistence.save(dto)

  def loadFromJson(): Unit = {
    // update values from datastore.json
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
      keyFieldMap.clear()
      keyNameMap.addAll(dto.namedKeys.map(namedKey => namedKey.key -> namedKey.name))
    }
    }
  }


}



//
//            case MessageContainer()
//
//            case ReplaceEntries(entries: Seq[FieldEntry])
//            =>
//
//            case All(replyTo: ActorRef[Seq[FieldEntry]]) =>
//              replyTo ! all.sorted
//            case AllForKey(key: Key, replyTo: ActorRef[Seq[FieldEntry]]) =>
//              replyTo ! all
//                .filter(_.fieldKey.key == key)
//                .sortBy(_.fieldKey.fieldName)
//            case AllForKeyKind(keyKind: KeyKind, replyTo: ActorRef[Seq[FieldEntry]]) =>
//              replyTo ! all.filter(_.fieldKey.key.keyKind == keyKind).sortBy(_.fieldKey)
//            case ForFieldKey(fieldKey: FieldKey, replyTo: ActorRef[Option[FieldEntry]]) =>
//              replyTo ! all.find(_.fieldKey == fieldKey)
//            case Candidates(replyTo: ActorRef[Seq[FieldEntry]]) =>
//              replyTo ! all.filter(_.candidate.nonEmpty)
//            case Triggers(replyTo: ActorRef[Seq[MacroWithTriggers]]) =>
//              val triggerNodes: Seq[TriggerNode] = all.filter { fe => fe.isInstanceOf[TriggerNode] }.map(_.asInstanceOf[TriggerNode])
//              replyTo !
//                (for {
//                  fieldEntry <- all.filter(_.fieldKey.key.keyKind == KeyKind.macroKey).sorted
//                  macroNode: RcMacro = fieldEntry.fieldValue.asInstanceOf[RcMacro]
//                } yield {
//                  val triggers: Seq[TriggerNode] = triggerNodes.filter(_.canRunMacro(macroNode.key))
//                  MacroWithTriggers(macroNode = macroNode, triggers = triggers)
//                })
//
//            case IngestJson(sJson, replyTo) =>
//              ingest(json.Json.parse(sJson).as[DataTransferJson])
//              replyTo ! "Done"
//
//            case Reload =>
//              load()
//            case Json(replyTo) =>
//              val dto = DataTransferJson(values = valuesMap.values.map(FieldEntryJson(_)).toSeq,
//                namedKeys = keyNamesMap.map { case (key, name) => NamedKey(key, name) }.toSeq)
//              replyTo ! persistence.toJson(dto)
//
//            case AcceptCandidate(fieldKey: FieldKey, user: User) =>
//              valuesMap.put(fieldKey, valuesMap(fieldKey).acceptCandidate())
//              save(user)
//            case ud: UpdateData =>
//              try
//                ud.candidates.foreach { candidate =>
//                  val fieldKey = candidate.fieldKey
//
//                  val currentEntry = valuesMap(candidate.fieldKey)
//                  val newEntry: FieldEntry = candidate.candidate match {
//                    case Left(formValue: String) =>
//                      currentEntry.setCandidate(formValue)
//
//                    case Right(value: ComplexFieldValue) =>
//                      currentEntry.setCandidate(value)
//                  }
//                  valuesMap.put(fieldKey, newEntry)
//                }
//                ud.namedKeys.foreach { namedKey =>
//                  val key = namedKey.key
//                  val name = namedKey.name
//                  if (name.isBlank)
//                    keyNamesMap.remove(key)
//                  else
//                    keyNamesMap.put(key, name)
//                }
//                save(ud.user)
//              catch
//                case e: Exception =>
//                  logger.error(ud.toString, e)
//              ud.replyTo ! "Done"
//            case UpdateFields(fieldEntries: Seq[FieldEntry], names: Seq[NamedKey], user: User) =>
//              fieldEntries.foreach { fieldEntry =>
//                valuesMap.put(fieldEntry.fieldKey, fieldEntry)
//              }
//              save(user)
//          }

