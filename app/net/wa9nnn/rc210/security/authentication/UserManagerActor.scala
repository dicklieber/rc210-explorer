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

package net.wa9nnn.rc210.security.authentication

import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Scheduler, Signal, SupervisorStrategy}
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import controllers.UserEditDTO
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import play.api.libs.concurrent.ActorModule

import java.nio.file.{Path, Paths}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps


@Singleton()
object UserManagerActor extends ActorModule with LazyLogging {
  sealed trait UserManagerMessage

  type Message = UserManagerMessage

  case class Users(replyTo: ActorRef[UserRecords]) extends UserManagerMessage

  case class Put(userDetailData: UserEditDTO, user: User, replyTo: ActorRef[String]) extends UserManagerMessage

  case class Remove(userId: UserId, user: User, replyTo: ActorRef[String]) extends UserManagerMessage

  case class Get(userId: UserId, replyTo: ActorRef[Option[User]]) extends UserManagerMessage

  case class Validate(credentials: Credentials, replyTo: ActorRef[Option[User]]) extends UserManagerMessage

  @Provides
  def apply(config: Config, defaultNoUsersLogin: DefaultNoUsersLogin)(implicit ec: ExecutionContext): Behavior[UserManagerMessage] = {
    val usersFile: Path = Paths.get( config.getString("vizRc210.usersFile"))

    val userManager = new UserManager(usersFile, defaultNoUsersLogin())
    Behaviors.supervise {
      Behaviors.setup[UserManagerMessage] { actorContext =>
        Behaviors.receiveMessage[Message] { message =>
            message match {
              case Put(userDetailData: UserEditDTO, user: User, replyTo: ActorRef[String]) =>
                userManager.put(userDetailData, user)
                replyTo ! "Added" // Don't need a return value but want client to wait until the actor is done.
              case Get(userId: UserId, replyTo: ActorRef[Option[User]]) =>
                val maybeUser: Option[User] = userManager.get(userId)
                replyTo ! maybeUser
              case Validate(login: Credentials, replyTo: ActorRef[Option[User]]) =>
                val maybeUser: Option[User] = userManager.validate(login)
                replyTo ! maybeUser
              case Users(replyTo: ActorRef[UserRecords]) =>
                replyTo ! userManager.users
              case Remove(userId: UserId, user: User, replyTo: ActorRef[String]) =>
                userManager.remove(userId, user.who)
                replyTo ! "Removed"
              case x =>
                logger.error(s"Unexpected message: $x!")
            }
            Behaviors.same
          }
          .receiveSignal {
            case (_, signal: Signal) =>
              logger.error(s"signal: $signal")
              //              if signal == PreRestart || signal == PostStop =>
              //          resource.close()
              Behaviors.same
          }

      }
    }.onFailure[Exception](SupervisorStrategy.restart)
  }
}