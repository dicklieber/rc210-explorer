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

import enumeratum.{EnumEntry, PlayEnum}
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.data.remotebase.CtcssMode.findValues
import net.wa9nnn.rc210.security.authentication.User

import scala.collection.concurrent.TrieMap

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

trait WriteRequst extends DataStoreRequest

case class ReplaceEntries(entries: Seq[FieldEntry]) extends DataStoreRequest

case object All extends DataStoreRequest {
  override def process(implicit data: TrieMap[FieldKey, FieldEntry]): Seq[FieldEntry] =
    data.values.toSeq
}

case class AllForKey(key: Key) extends DataStoreRequest

case class AllForKeyKind(keyKind: KeyKind) extends DataStoreRequest

case class ForFieldKey(fieldKey: FieldKey) extends DataStoreRequest

case object Json extends DataStoreRequest

case class IngestJson(sJson: String) extends WriteRequst

case object Candidates extends DataStoreRequest

case object Triggers extends DataStoreRequest

case class AcceptCandidate(fieldKey: FieldKey, user: User) extends WriteRequst


case class UpdateData(candidates: Seq[UpdateCandidate], namedKeys: Seq[NamedKey] = Seq.empty) extends WriteRequst

case object Reload extends DataStoreRequest

case class CandidateAndNames(candidates: Seq[UpdateCandidate], namedKeys: Seq[NamedKey] = Seq.empty) extends WriteRequst

object CandidateAndNames:
  def apply(updateCandidate: UpdateCandidate, maybeNamedKey: Option[NamedKey]): CandidateAndNames = {
    new CandidateAndNames(Seq(updateCandidate), maybeNamedKey.toIndexedSeq)
  }


import net.wa9nnn.rc210.ui.{EnumEntryValue, EnumValue}

sealed trait SendField(val dataStoreRequest: DataStoreRequest) extends EnumEntry:
  override def values: IndexedSeq[EnumEntry] = values

object SendField extends PlayEnum[SendField]:

  override val values: IndexedSeq[SendField] = findValues

  case object AllFields extends SendField(All)

  case object CandidatesOnly extends SendField(Candidates)


