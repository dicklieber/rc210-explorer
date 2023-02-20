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

package net.wa9nnn.rc210.data.duration

import play.api.libs.json.{JsValue, Json, OFormat}

import java.time.Duration

trait RCDuration {
  def duration: Duration

  /**
   * RC-210 using integers for various durations.
   *
   * @return
   */
  def toInteger: Long
}


case class Minutes(duration: Duration) extends RCDuration {
  /**
   * based on the unit of each type.
   *
   * @return
   */
  override def toInteger: Long = duration.toMinutes
}

case class Seconds(duration: Duration) extends RCDuration {
  /**
   * based on the unit of each type.
   *
   * @return
   */
  override def toInteger: Long = duration.toSeconds
}

case class TenthSeconds(duration: Duration) extends RCDuration {
  /**
   * based on the unit of each type.
   *
   * @return
   */
  override def toInteger: Long = duration.toMillis / 10
}

case class Milliseconds(duration: Duration) extends RCDuration {
  /**
   * based on the unit of each type.
   *
   * @return
   */
  override def toInteger: Long = duration.toMillis
}

object RCDuration {
  def minutes(s: String): Minutes = new Minutes(Duration.ofMinutes(s.toLong))

  def seconds(s: String): Seconds = new Seconds(Duration.ofSeconds(s.toLong))

  def tenthSeconds(s: String): TenthSeconds = new TenthSeconds(Duration.ofMillis(s.toLong / 10))

  def millis(s: String): Milliseconds = new Milliseconds(Duration.ofMillis(s.toLong))

//  implicit val fmtMinutes: OFormat[Minutes] = new OFormat[Minutes] {
//    override def reads(json: JsValue) = ???
//
//    override def writes(o: Minutes)JsValue = new JsValue(
//
//    )
//  } {
//  }
}
