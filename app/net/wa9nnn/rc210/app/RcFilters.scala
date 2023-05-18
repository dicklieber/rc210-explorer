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

package net.wa9nnn.rc210.app

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.security.authorzation.{AuthenticationFilter, LoggingFilter}

import javax.inject.{Inject, Singleton}
import play.api.http.EnabledFilters
import play.filters.gzip.GzipFilter
import play.api.http.DefaultHttpFilters

class RcFilters @Inject()(
                         log: LoggingFilter,
                         authenticationFilter: AuthenticationFilter
                       ) extends DefaultHttpFilters(authenticationFilter, log)
  with LazyLogging {
  logger.info(s"log: $log")
  logger.info(s"authenticationFilter: $authenticationFilter")
}

