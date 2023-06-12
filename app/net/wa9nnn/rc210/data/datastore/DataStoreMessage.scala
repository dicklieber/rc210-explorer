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

import akka.actor.typed.ActorRef
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.key.KeyKind
import net.wa9nnn.rc210.security.authentication.User
import net.wa9nnn.rc210.serial.comm.Progress
import net.wa9nnn.rc210.ui.CandidateAndNames

import scala.collection.immutable.Seq

 trait DataStoreMessage


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

  case class RC210Result(mainArray: Seq[Int], extArray: Seq[Int], progress: Progress) extends DataStoreMessage


  case class UpdateData(candidates: Seq[UpdateCandidate], namedKeys: Seq[NamedKey] = Seq.empty, user: User, replyTo: ActorRef[String]) extends DataStoreMessage

  object UpdateData {
   def apply(candidateAndNames: CandidateAndNames, user: User, replyTo: ActorRef[String]): UpdateData = {
    new UpdateData(candidateAndNames.candidates, candidateAndNames.namedKeys, user, replyTo)
   }
  }

  case class StartDownload(descriptor: String) extends DataStoreMessage

