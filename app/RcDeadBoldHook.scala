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

import be.objectify.deadbolt.scala.cache.HandlerCache
import net.wa9nnn.rc210.security.authorzation.RcHandlerCache
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

class RcDeadBoldHook extends Module {
  override def bindings(environment: Environment,
                        configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[HandlerCache].to[RcHandlerCache]
  )
}
