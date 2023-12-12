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
import net.wa9nnn.rc210.security.authorzation.AuthFilter.session
import play.api.mvc.Request

import javax.inject.Singleton
import scala.collection.concurrent.TrieMap
import scala.util.Try

/**
 * This is the in-memory source of all RC-210 and NamedKey data.
 */
@Singleton
class DataStore(persistence: DataStorePersistence) extends NamedKeySource {
  private implicit val keyFieldMap: TrieMap[FieldKey, FieldEntry] = new TrieMap[FieldKey, FieldEntry]()
  private val keyNameMap = new TrieMap[Key, String]
  Key.setNamedSource(this) // so any Key can get it's user-supplied name.

  loadFromJson()

  def apply(askRequest: DataStoreRequest)(implicit request: Request[_]): DataStoreReply = {
    val dataStoreReply: DataStoreReply = DataStoreReply(Try(askRequest.process))
    askRequest match
      case _: WriteRequst => save(session)
      case _ =>
    dataStoreReply
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

  def reload():Unit =
    throw new NotImplementedError() //todo
    
  def save(session: RcSession): Unit =
    val dto = toJson.copy(who = Some(session.user.who))
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

  def toJson: DataTransferJson =
    DataTransferJson(values = 
      keyFieldMap.values.map(FieldEntryJson(_)).toSeq,
      namedKeys = keyNameMap.map { case (key, name) => NamedKey(key, name) }.toSeq)

}


