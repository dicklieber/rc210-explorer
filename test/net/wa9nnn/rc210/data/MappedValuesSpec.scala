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

package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.PortKey
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class MappedValuesSpec extends Specification {
  "MappedValues" >> {
    val fieldName = "fieldA"
    val fieldMetadataA = FieldMetadata(fieldName, "8300")
    val initialValue = "-initial-"

    "Happy Path" >> {
       val mappedValues = new MappedValues(PortKey(2))
      mappedValues.setupField( fieldMetadataA, initialValue)

      val fieldContainer: FieldContainer = mappedValues.container(fieldName)
      fieldContainer.value must beEqualTo(initialValue)
      fieldContainer.state.candidate must beNone
      // Update the value
      val value2 = "Value 2"
      mappedValues.update(fieldName, value2)
      val fieldState = mappedValues.container(fieldName).state
      fieldState.value must beEqualTo(initialValue)
      fieldState.candidate must beSome(value2)

      // Accept the value
      mappedValues.acceptCandidate(fieldName)
      val fieldStateAccepted: FieldState = mappedValues.container(fieldName).state
      fieldStateAccepted.value must beEqualTo(value2)
      fieldStateAccepted.candidate must beNone
    }
    "toJson" >> {
      val mappedValues = new MappedValues((PortKey(2)))
      mappedValues.setupField(fieldMetadataA, initialValue)
      mappedValues.setupField(FieldMetadata("field2", "1234"), "xyzzy")
      val json = Json.toJson(mappedValues)
      val sJson = Json.prettyPrint(json)
sJson must beEqualTo ("""{
                        |  "key" : "port2",
                        |  "values" : [ [ {
                        |    "metadata" : {
                        |      "name" : "field2",
                        |      "command" : "1234"
                        |    },
                        |    "fieldState" : {
                        |      "value" : "xyzzy"
                        |    }
                        |  }, {
                        |    "metadata" : {
                        |      "name" : "fieldA",
                        |      "command" : "8300"
                        |    },
                        |    "fieldState" : {
                        |      "value" : "-initial-"
                        |    }
                        |  } ] ]
                        |}""".stripMargin)
    }
  }
}
