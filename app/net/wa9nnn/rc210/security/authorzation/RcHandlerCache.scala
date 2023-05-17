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

package net.wa9nnn.rc210.security.authorzation

import be.objectify.deadbolt.scala.{DeadboltHandler, HandlerKey}
import be.objectify.deadbolt.scala.cache.HandlerCache

import javax.inject.{Inject, Singleton}

@Singleton
class RcHandlerCache @Inject()(rcDeadboltHandler: RcDeadboltHandler) extends HandlerCache {
  val defaultHandler: DeadboltHandler = rcDeadboltHandler

  // Get the default handler.
  override def apply(): DeadboltHandler = defaultHandler

  // Get a named handler
  override def apply(handlerKey: HandlerKey): DeadboltHandler = {
    defaultHandler
  }
}
