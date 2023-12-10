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

import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.authentication.{RcSession, User}
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.ActorRef

import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success, Try}

/**
 * Message that the Datastore can process.
 */
sealed trait DataStoreRequest:
  /**
   * 
   * @param data can throw on error
   * @return
   */
  def process(implicit data: TrieMap[FieldKey, FieldEntry]): Seq[FieldEntry] = throw new NotImplementedError() //todo
end DataStoreRequest

case class ReplaceEntries(entries: Seq[FieldEntry]) extends DataStoreRequest

case object All extends DataStoreRequest {
  override def process(implicit data: TrieMap[FieldKey, FieldEntry]): Seq[FieldEntry] =
    data.values.toSeq
}

case class AllForKey(key: Key) extends DataStoreRequest

case class AllForKeyKind(keyKind: KeyKind) extends DataStoreRequest

case class ForFieldKey(fieldKey: FieldKey) extends DataStoreRequest

case object Json extends DataStoreRequest

case class IngestJson(sJson: String) extends DataStoreRequest

case object Candidates extends DataStoreRequest

case object Triggers extends DataStoreRequest

case class AcceptCandidate(fieldKey: FieldKey, user: User) extends DataStoreRequest


case class UpdateData(candidates: Seq[UpdateCandidate], namedKeys: Seq[NamedKey] = Seq.empty) extends DataStoreRequest

case object Reload extends DataStoreRequest

case class DataStoreMessage(dataStoreRequest: DataStoreRequest, rcSession: RcSession, replyTo: ActorRef[DataStoreReply])

case class CandidateAndNames(candidates: Seq[UpdateCandidate], namedKeys: Seq[NamedKey] = Seq.empty) extends DataStoreRequest

//case class UpdateFields(fieldEntries: Seq[FieldEntry], names: Seq[NamedKey] = Seq.empty) extends DataStoreRequest

object CandidateAndNames:
  def apply(updateCandidate: UpdateCandidate, maybeNamedKey: Option[NamedKey]): CandidateAndNames = {
    new CandidateAndNames(Seq(updateCandidate), maybeNamedKey.toIndexedSeq)
  }



