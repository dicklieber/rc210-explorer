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

import com.google.inject.Provides
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.TriggerNode
import net.wa9nnn.rc210.data.datastore.DataStoreActor.Message
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldKey, FieldValue}
import net.wa9nnn.rc210.data.macros.RcMacro
import net.wa9nnn.rc210.data.named.{NamedKey, NamedKeySource}
import net.wa9nnn.rc210.security.authentication.User
import net.wa9nnn.rc210.{Key, KeyKind}
import org.apache.pekko.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import org.apache.pekko.actor.typed.{ActorRef, Behavior, PostStop, Signal, SupervisorStrategy}
import play.api.libs.concurrent.ActorModule
import play.api.libs.json

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}


class DataStoreActor(context: ActorContext[DataStoreMessage], dataStoreLogic: DataStoreLogic)
  extends AbstractBehavior[Message](context) with LazyLogging {
  logger.info("DataStoreActor started")

  override def onMessage(dataStoreMessage: DataStoreMessage): Behavior[DataStoreMessage] = {
    val result: DataStoreReply = dataStoreLogic(dataStoreMessage.dataStoreRequest)
    dataStoreMessage.replyTo ! result
    this
  }

  override def onSignal: PartialFunction[Signal, Behavior[DataStoreMessage]] = {
    case PostStop =>
      logger.error("DataStoreActor stoped")
      this
  }
}


object DataStoreActor extends ActorModule with LazyLogging with NamedKeySource {
  type Message = DataStoreMessage


  @Provides def apply(implicit persistence: DataStorePersistence, ec: ExecutionContext): Behavior[Message] = {
    val dataStoreLogic = new DataStoreLogic(persistence)

    Behaviors.setup[Message](context =>
      new DataStoreActor(context, dataStoreLogic)
    )
  }
}

