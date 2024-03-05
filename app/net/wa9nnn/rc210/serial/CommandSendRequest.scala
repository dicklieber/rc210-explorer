/*
 * Copyright (C) 2024  Dick Lieber, WA9NNN                               
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

package net.wa9nnn.rc210.serial

import net.wa9nnn.rc210.FieldKey
import play.api.libs.json.{Format, Json}
import play.api.mvc.PathBindable

import scala.util.matching.Regex

case class CommandSendRequest(sendField: SendField, fieldKey: Option[FieldKey] = None) {
  override def toString: String = {
    val value = s"$sendField~${fieldKey.map(_.toString).getOrElse("")}"
    value
  }
}

object CommandSendRequest:
  def apply(sendField: SendField, fieldKey: FieldKey): CommandSendRequest =
    CommandSendRequest(sendField, Option(fieldKey))

  private val r: Regex = """(\S+)~(\S+)?""".r

  def apply(s: String): CommandSendRequest = {
    s match
      case r(sf, fk) =>
        val sendField = SendField.withName(sf)
        val maybeFieldKey:Option[FieldKey] = Option(fk).map(FieldKey.fromId)
        CommandSendRequest(sendField, maybeFieldKey)
      case x =>
        throw  new IllegalArgumentException(s"Can't parse $x")
  }

  implicit val fmtCommandSendRequest: Format[CommandSendRequest] = Json.format[CommandSendRequest]

  implicit def pathBinder: PathBindable[CommandSendRequest] = new PathBindable[CommandSendRequest] {
    override def bind(key: String, value: String): Either[String, CommandSendRequest] =

      try {
        Right(CommandSendRequest(value))
      } catch {
        case e: Exception =>
          Left(e.getMessage)
      }

    override def unbind(key: String, commandSendRequest: CommandSendRequest): String =
      commandSendRequest.toString
  }
