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
import net.wa9nnn.rc210.data.mapped.{FieldContainer, FieldState, MappedValues}
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class MappedValuesSpec extends Specification {
  "MappedValues" >> {
    val fieldName = "fieldA"
    val portKey = PortKey(3)
    val fieldKey = FieldKey(fieldName, portKey)
    val fieldMetadataA = FieldMetadata(fieldKey, "8300")
    val initialValue = "-initial-"

    "Happy Path" >> {
       val mappedValues = new MappedValues()
      mappedValues.setupField( fieldMetadataA, initialValue)

      val fieldContainer: FieldContainer = mappedValues.container(fieldKey)
      fieldContainer.value must beEqualTo(initialValue)
      fieldContainer.state.candidate must beNone
      // Update the value
      val value2 = "Value 2"
      mappedValues.update(fieldKey, value2)
      val fieldState = mappedValues.container(fieldKey).state
      fieldState.value must beEqualTo(initialValue)
      fieldState.candidate must beSome(value2)

      // Accept the value
      mappedValues.acceptCandidate(fieldKey)
      val fieldStateAccepted: FieldState = mappedValues.container(fieldKey).state
      fieldStateAccepted.value must beEqualTo(value2)
      fieldStateAccepted.candidate must beNone
    }
    "toJson" >> {
      val mappedValues = new MappedValues()
      mappedValues.setupField(fieldMetadataA, initialValue)
      val fieldKey2 = FieldKey("field2", portKey)
      mappedValues.setupField(FieldMetadata(fieldKey2, "1234"), "xyzzy")
      val json = Json.toJson(mappedValues)
      val sJson = Json.prettyPrint(json)
sJson must beEqualTo ("""{
                        |  "values" : [ [ {
                        |    "metadata" : {
                        |      "fieldKey" : "fieldA|port3",
                        |      "command" : "8300"
                        |    },
                        |    "fieldState" : {
                        |      "value" : "-initial-"
                        |    }
                        |  }, {
                        |    "metadata" : {
                        |      "fieldKey" : "field2|port3",
                        |      "command" : "1234"
                        |    },
                        |    "fieldState" : {
                        |      "value" : "xyzzy"
                        |    }
                        |  } ] ]
                        |}""".stripMargin)
    }
  }
}
