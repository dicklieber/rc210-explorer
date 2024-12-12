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

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row, RowSource, TableSection}
import net.wa9nnn.rc210.{FieldKey, Key}
import play.api.libs.json.{Format, JsObject, JsResult, JsValue, OFormat}

/**
 * Holds the value for a rc2input.
 * Knows how to render as HTML control or string for JSON, showing to a user or RC-210 Command,
 * Has enough metadata needed yo render
 */
sealed trait FieldValue extends LazyLogging with RowSource :
  /**
   * Nodes that actually have an enabled field should override this.
   *
   * @return
   */
  def enabled: Boolean = true

  def runableMacros: Seq[Key]

  def canRunMacro(candidate: Key): Boolean =
    enabled && runableMacros.contains(candidate)
  def canRunAny:Boolean =
    runableMacros.nonEmpty

  def tableSection(fieldKey: FieldKey):TableSection =
    throw new NotImplementedError() //todo


  /**
   * Render this value as an RC-210 command string.
   */
  def toCommands(fieldEntry: FieldEntryBase): Seq[String]

  /**
   * Read only HTML display
   */
  def displayCell: Cell

  /**
   * A form input field
   *
   * @param fieldKey
   */
  def toEditCell(fieldKey: FieldKey): Cell =
    throw new NotImplementedError() //todo

  def toJsValue: JsValue

object FieldValue:
  implicit val fmt: OFormat[FieldValue] = new OFormat[FieldValue] {
    override def writes(o: FieldValue): JsObject =
      o.toJsValue.as[JsObject]

    override def reads(json: JsValue): JsResult[FieldValue] =
      throw new NotImplementedError() //todo
  }

/**
 * Renders itself as a [[[Cell]]
 */
trait SimpleFieldValue(val runableMacros: Key*) extends FieldValue {
  /**
   *
   * @param paramValue candidate from form.
   * @return None if value has not changed, otherwise a new [[FieldValue]].
   */
  def update(paramValue: String): SimpleFieldValue

}

trait ComplexFieldValue(val runableMacros: Key*) extends FieldValue with LazyLogging :
  val key: Key

  lazy val fieldKey: FieldKey = FieldKey(key)








