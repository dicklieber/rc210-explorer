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

package controllers

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.ui.Tabs
import net.wa9nnn.rc210.ui.nav.TabKind
import play.api.mvc.*
import views.html.{NavMain, landing}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class NavigationController @Inject()(implicit dataStore: DataStore, ec: ExecutionContext,
                                     components: MessagesControllerComponents,
                                     navMain: NavMain)
  extends MessagesAbstractController(components) with LazyLogging {

  def selectTabKind(tabKind: TabKind): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] => {
      logger.debug(tabKind.toString)
      Ok(navMain(tabKind.noTab,landing()))
    }
  }
}



