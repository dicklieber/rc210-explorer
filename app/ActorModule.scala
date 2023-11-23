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
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.security.authentication.{SessionManagerActor, UserManagerActor}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Terminated}
import play.api.libs.concurrent.{ActorModule, PekkoGuiceSupport}
//import play.api.libs.concurrent.{ActorModule, AkkaGuiceSupport}

import scala.concurrent.ExecutionContext

object RcActorsModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bindTypedActor[DataStoreActor.Message](DataStoreActor, "dataStore-actor")

    bindTypedActor[Supervisor.Message](Supervisor, "supervisor")

    bindTypedActor[SessionManagerActor.Message](SessionManagerActor, "sessionManager-actor")
    bindTypedActor[UserManagerActor.Message](UserManagerActor, "userManager-actor")
  }

}

object Supervisor extends ActorModule with LazyLogging {
  trait RcSupervisor

  override type Message = RcSupervisor

  @Provides def apply(config: Config)(implicit ec: ExecutionContext,
                                      dataStoreActor: ActorRef[DataStoreActor.Message],
                                      sessionActor: ActorRef[SessionManagerActor.Message],
                                      userActor: ActorRef[UserManagerActor.Message])= {
    Behaviors.setup[Message] { actorContext =>
      actorContext.watch(dataStoreActor)
      actorContext.watch(sessionActor)
      actorContext.watch(userActor)

//      Behaviors.receiveMessage { message: Message =>
//        logger.info(s"Supervisor: $message")
//        message match {
//          case x =>
//            logger.info(s"x: $x")
//        }
//        Behaviors.same
//      }
      Behaviors.receiveSignal {
        case (context, Terminated(ref: ActorRef[Nothing])) =>
          context.log.info("Job stopped: {}", ref.path.name)
          Behaviors.same
      }
    }
  }
}

