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

import net.wa9nnn.rc210.data.field.RenderMetadata
import net.wa9nnn.rc210.key.KeyFactory
import net.wa9nnn.rc210.key.KeyFactory.CourtesyToneKey

import scala.util.matching.Regex

 class CtSegmentKey(val ctKey: CourtesyToneKey, val segment: Int, val name:String, override val units:String = "Hz") extends RenderMetadata {
  override def param: String = s"$name.$segment.${ctKey.toString}"
}

object CtSegmentKey {

  def apply(name:String, segment: Int, units:String = "Hz")(implicit  ctSegmentKey: CourtesyToneKey) : CtSegmentKey = {
    new CtSegmentKey(ctSegmentKey, segment, name, units)
  }

  val r: Regex = """(.+)\.(\d+)\.(.+)""".r
  def apply(s:String):CtSegmentKey = {
    val r(name, sSegment, sKey) = s
    new CtSegmentKey(KeyFactory(sKey),sSegment.toInt , name)
  }
}