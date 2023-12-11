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
import net.wa9nnn.rc210.security.authorzation.AuthFilter.session
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.ActorRef
import play.api.mvc.Request

import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success, Try}



case class DataStoreMessage(dataStoreRequest: DataStoreRequest, rcSession: RcSession, replyTo: ActorRef[DataStoreReply])

object DataStoreMessage:
  def apply(dataStoreRequest: DataStoreRequest, replyTo: ActorRef[DataStoreReply])(implicit request:Request[_])=
    new DataStoreMessage(dataStoreRequest, session, replyTo)
end DataStoreMessage


