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

package net.wa9nnn.rc210.data.field

import org.specs2.mutable.Specification

class FieldDtmfSpec extends Specification {

  "FieldDtmf" should {
    "extractFromInts" in {
      val fieldDefinitions = new FieldDefinitions()
      val remoteBaseDefinition: SimpleField = fieldDefinitions.simpleFields.find(_.fieldName == "Remote Base Prefix").get
      val ints: Seq[Int] = Seq(53,0,0,255,255,255)
      val fieldValue: FieldDtmf = FieldDtmf.extractFromInts(ints.iterator, remoteBaseDefinition).asInstanceOf[FieldDtmf]
      fieldValue.display must beEqualTo ("5")

    }
  }
}
