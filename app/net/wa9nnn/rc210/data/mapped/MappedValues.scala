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
import net.wa9nnn.rc210.data.field.{FieldValue, FieldEntry, ComplexFieldValue}
import net.wa9nnn.rc210.key.KeyFactory.Key
import net.wa9nnn.rc210.key.KeyKind
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


  def apply(keyKind: KeyKind): Seq[FieldEntry] = {
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

  def apply(fieldKey: FieldKey, fieldContents: FieldValue): Unit = {
    val entry: FieldEntry = map.getOrElse(fieldKey, throw new IllegalArgumentException(s"No value for $fieldKey"))
    map.put(fieldKey, entry.copy(candidate = Option(fieldContents)))
  }

  def apply(newCandidate: NewCandidate): Unit = {
    val fieldKey = newCandidate.fieldKey
    val fieldEntry: FieldEntry = map.apply(fieldKey)
    val updatedFieldEntry: FieldEntry = fieldEntry.setCandidate(newCandidate.formValue)
    map.put(fieldKey, updatedFieldEntry)
  }

  def apply(newValues: Iterable[NewCandidate]): Unit = {
    newValues.foreach(newCandidate => apply(newCandidate))
  }

  def apply[K <: Key](fields: Seq[ComplexFieldValue[K]]): Unit = {
    fields.foreach { fieldWithFieldKey =>
      apply(fieldWithFieldKey.fieldKey, fieldWithFieldKey)
    }
  }

}

object MappedValues {

  import play.api.libs.json._

  implicit val fmtMappedValues: Format[MappedValues] = new Format[MappedValues] {
    override def reads(json: JsValue): JsResult[MappedValues] = ???

    override def writes(o: MappedValues): JsValue = ???
  }
}


case class NewCandidate(fieldKey: FieldKey, formValue: String)
