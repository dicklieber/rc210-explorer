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

import com.google.inject.AbstractModule
import net.wa9nnn.rc210.data.datastore.DataStoreActor
import net.wa9nnn.rc210.security.authentication.{SessionManager, SessionManagerActor, UserManagerActor}
import play.api.libs.concurrent.AkkaGuiceSupport

object ActorModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindTypedActor[SessionManagerActor.Message](SessionManagerActor, "sessionManager-actor")
    bindTypedActor[UserManagerActor.Message](UserManagerActor, "userManager-actor")
    bindTypedActor[DataStoreActor.Message](DataStoreActor, "dataStore-actor")
  }
}



