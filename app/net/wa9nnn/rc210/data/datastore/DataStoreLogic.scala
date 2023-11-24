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
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.authentication.User

import scala.collection.concurrent.TrieMap

/**
 * The stuff behind the [[DataStoreActor]].
 */
class DataStoreLogic(persistence: DataStorePersistence) {
  private val data: TrieMap[FieldKey, FieldEntry] = new TrieMap[FieldKey, FieldEntry]()

  def all: Seq[FieldEntry] = data.values.toSeq

  def entries(keyKind: KeyKind): Seq[FieldEntry] =
    all.filter(_.fieldKey.key.keyKind == keyKind).sortBy(_.fieldKey)

  def entries(key: Key): Seq[FieldEntry] =
    all.filter(_.fieldKey.key == key).sortBy(_.fieldKey.fieldName)

  def withCandidate: Seq[FieldEntry] = all.filter(_.candidate.nonEmpty).sortBy(_.fieldKey.fieldName)

  def triggerNodes: Seq[TriggerNode] =
    all.flatMap { fieldEntry =>
      fieldEntry match
        case tn: TriggerNode =>
          Option.when(tn.nodeEnabled)(tn)
        case _ =>
          Seq.empty
    }

  def acceptCandidate(fieldKey: FieldKey, user: User) =
    data.put(fieldKey, data(fieldKey).acceptCandidate())
    save(user)


  private def save(user: User): Unit =
    throw new NotImplementedError() //todo
  //    val dto = DataTransferJson(values = data.values.map(FieldEntryJson(_)).toSeq,
  //      namedKeys = data.map { case (key, name) => NamedKey(key, name) }.toSeq,
  //      who = Option(user.who))
  //    persistence.save(dto)


}
