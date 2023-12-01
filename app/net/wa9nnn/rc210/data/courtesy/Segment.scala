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

package net.wa9nnn.rc210.data.courtesy

import com.typesafe.scalalogging.LazyLogging
import play.api.data.Forms.*
import play.api.data.Mapping
import play.api.libs.json.{Format, Json}

/**
 * One piece of a [[CourtesyTone]].
 *
 * @param delayMs until this segment starts.
 * @param durationMs how long.
 * @param tone1Hz base tone.
 * @param tone2Hz mixed with [[tone1Hz]].,
 */
case class Segment(delayMs: Int, durationMs: Int, tone1Hz: Int, tone2Hz: Int):
  def toCommand(number: Int, segN: Int): String = {
    //1*31011200*100*6
    val sNumber = f"$number%02d"

    val spaced = s"1*3$segN$sNumber $delayMs * $durationMs * $tone1Hz * $tone2Hz*"
    spaced.replace(" ", "")
  }

  def tone(toneNo: Int): Int = toneNo match
    case 1 => tone1Hz
    case 2 => tone2Hz

object Segment extends LazyLogging:
  def unapply(u: Segment): Option[(Int, Int, Int, Int)] = Some((u.delayMs, u.durationMs, u.tone1Hz, u.tone2Hz))

  val form: Mapping[Segment] =
    mapping(
      "delayMs" -> number,
      "durationMs" -> number,
      "tone1Hz" -> number,
      "tone2Hz" -> number,
    )(Segment.apply)(Segment.unapply)

  implicit val fmtSegment: Format[Segment] = Json.format[Segment]
