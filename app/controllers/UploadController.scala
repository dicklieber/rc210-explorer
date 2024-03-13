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

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.serial.*
import net.wa9nnn.rc210.ui.TabE.RC210Download
import net.wa9nnn.rc210.ui.{TabE, Tabs}
import org.apache.pekko.stream.Materializer
import play.api.mvc.*
import views.html.NavMain

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

/**
 *  Sends data to the RC-210 
 */
@Singleton()
class UploadController @Inject()(config: Config, commandsSender: CommandsSender,
                                 rc210: Rc210, navMain: NavMain)
                                (implicit ec: ExecutionContext, mat: Materializer, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) with LazyLogging {
  
  def start(commandSendRequest: UploadRequest): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>

      commandsSender.newUpload(commandSendRequest)
      
      val webSocketURL: String = controllers.routes.UploadController.ws().webSocketURL()
      Ok(navMain(RC210Download, views.html.progress(webSocketURL, commandSendRequest.table)))
  }

  def ws(): WebSocket =
    new ProcessWithProgress[FieldResult](1)(progressApi =>
      commandsSender.startDownload(progressApi)).webSocket

}

