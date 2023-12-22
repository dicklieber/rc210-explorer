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

package net.wa9nnn.rc210.security

import net.wa9nnn.rc210.security.UserId.UserId
import net.wa9nnn.rc210.security.Who.Callsign
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import play.api.libs.json.{Json, OFormat}
import play.api.mvc
import play.api.mvc.{AnyContent, MessagesRequest, Request}

case class Who(callsign: Callsign = "unknown", id: UserId = UserId.none, email: String = "") extends Ordered[Who] {
  override def compare(that: Who): Int = this.callsign compareTo that.callsign
}

object Who:
  implicit val whoFmt: OFormat[Who] = Json.format
  type Callsign = String

  def session(request: MessagesRequest[AnyContent]): RcSession =
    request.attrs(sessionKey)

  given request2Session: Conversion[MessagesRequest[AnyContent], RcSession] with
    def apply(request: MessagesRequest[AnyContent]): RcSession =
      request.attrs(sessionKey)
  




