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
import net.wa9nnn.rc210.data.courtesy.Segment.logger


case class Segment(delayMs: Int, durationMs: Int, tone1Hz: Int, tone2Hz: Int) {
  def toCommand(number: Int, segN: Int): String = {
    //1*31011200*100*6
    val sNumber = f"$number%02d"

    val spaced = s"1*3$segN$sNumber $delayMs * $durationMs * $tone1Hz * $tone2Hz*"
    spaced.replace(" ", "")
  }
}

object Segment extends LazyLogging {
  def apply(m: Map[String, String]): Segment = {
    logger.trace(s"m: $m")
    try {
      val delay = m("Delay").toInt
      val duration = m("Duration").toInt
      val tone1 = m("Tone1").toInt
      val tone2 = m("Tone2").toInt

      new Segment(delay, duration, tone1, tone2)
    } catch {
      case e: Exception =>
        logger.error("Creating a Segment", e)
        throw e
    }
  }
}