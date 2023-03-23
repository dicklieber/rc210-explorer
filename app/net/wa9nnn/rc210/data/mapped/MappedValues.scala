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

package net.wa9nnn.rc210.data.mapped

import net.wa9nnn.rc210.DataProvider
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.{FieldContents, FieldEntry}
import net.wa9nnn.rc210.key.{Key, KeyKind}
import play.api.libs.json.JsArray

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap

/**
 * Holds most values are simple key->value.
 * Does not include more complex fields e.g.Macro
 */
@Singleton
class MappedValues @Inject()(dataProvider: DataProvider) {
  private val map = new TrieMap[FieldKey, FieldEntry]

  dataProvider.initialValues.foreach { fieldContents =>
    map.put(fieldContents.fieldKey, fieldContents)
  }

  def all: Seq[FieldEntry] = {
    map.values.toSeq.sorted
  }


  def apply(keyKind: KeyKind):Seq[FieldEntry]={
  map.values.filter(_.fieldKey.key.kind == keyKind)
    .toSeq
    .sortBy(_.fieldKey)
  }
  def apply(fieldKey: FieldKey): Option[FieldEntry] = {
    map.values.find(_.fieldKey == fieldKey)
  }

  /**
   *
   * @param key of interest
   * @return order by field name.
   */
  def apply(key: Key): Seq[FieldEntry] = {
    map.values
      .filter(_.fieldKey.key == key)
      .toSeq
      .sortBy(_.fieldKey.fieldName)
  }

  def acceptCandidate(fieldKey: FieldKey): Unit = {
    map.put(fieldKey, map(fieldKey).acceptCandidate())
  }

  def apply(fieldKey: FieldKey, value: String): Unit = {
    val entry: FieldEntry = map(fieldKey)
    map.put(fieldKey, entry.setCandidate(value))
  }
  def apply(fieldKey: FieldKey, fieldContents: FieldContents): Unit = {
    val entry: FieldEntry = map.getOrElse(fieldKey,throw new IllegalArgumentException(s"N value for $fieldKey"))
    map.put(fieldKey, entry.copy(fieldValue = fieldContents)) // todo candidate
  }

  def toJson: JsArray = {
    throw new NotImplementedError() //todo
    //    import play.api.libs.json._
    //    Json.arr(
    //      valueMap.values
    //    )
  }
}

object MappedValues {

  import play.api.libs.json._

  implicit val fmtMappedValues: OFormat[MappedValues] = new OFormat[MappedValues] {

    override def writes(mappedValues: MappedValues): JsObject = {
      JsObject(
        mappedValues
          .all
          .map(fe => fe.fieldKey.param -> fe.toJson)
      )
    }

    override def reads(json: JsValue): JsResult[MappedValues] = {
      throw new NotImplementedError() //todo
      //      JsResult.fromTry(Try {
      //        val values: Seq[FieldValue] = json.as[Seq[FieldValue]]
      //        values.foreach(fieldMetadata =>
      //          map.put(fieldMetadata.fieldKey, fieldMetadata)
      //        )
      //        mappedValues
      //      }
      //      )
    }
  }

}



