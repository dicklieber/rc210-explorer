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
import enumeratum.EnumEntry
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.clock.MonthOfYearDST
import net.wa9nnn.rc210.data.clock.MonthOfYearDST.values
import net.wa9nnn.rc210.ui.KeyedRow
import play.api.libs.json.{Format, JsResult, JsValue, Json}
import play.twirl.api.Html

import scala.xml.*

/**
 * Holds the value for a rc2input.
 * Knows how to render as HTML control or string for JSON, showing to a user or RC-210 Command,
 * Has enough metadata needed to render
 */
sealed trait FieldValue extends LazyLogging:
  /**
   * Nodes that actually have an enabled field should override this.
   * Used to determine if macro keys are runnable.
   *
   * @return
   */
  def enabled: Boolean = true

  def runableMacros: Seq[Key] = Seq.empty

  def canRunMacro(candidate: Key): Boolean =
    enabled && runableMacros.contains(candidate)

  def canRunAny: Boolean =
    runableMacros.nonEmpty

  def tableSection(key: Key): TableSection =
    throw new NotImplementedError() //todo

  /**
   * Read only HTML display
   */
  def displayCell: Cell

object FieldValue:
  implicit val fmt: Format[FieldValue] = new Format[FieldValue]:
    override def reads(json: JsValue) =
      throw new NotImplementedError() //todo

    override def writes(o: FieldValue) =
      Json.obj(
        "type" -> o.getClass.getSimpleName
      )

/**
 * Renders itself as a [[[Cell]]
 */
trait FieldValueSimple(override val runableMacros: Key*) extends FieldValue with RowSource:
  def toEditCell(key: Key): Cell

  def toCommand(key: Key, template: String): String

trait FieldValueComplex[T <: FieldValueComplex[?]](override val runableMacros: Key*)
  extends FieldValue with KeyedRow with LazyLogging:

  def toCommands(key: Key): Seq[String]

/**
 * A [[FieldValue]] that is an Enumeratium entry.
 */
trait Rc210EnumEntry extends EnumEntry with FieldValue:
  val rc210Value: Int
  val vals: Seq[Rc210EnumEntry]

  def options: Seq[(String, String)] = vals.map(v =>
    val s = v.entryName
    s -> s
  )

  override def displayCell: Cell = Cell(entryName)

//  def selectControl(key: Key): Html =
//    val id = key.id
//    val s = <select name={id} id={id} class="" style="width-auto">
//      {options.foreach { option =>
//        <option value={option._1}>
//          {option._1}
//        </option>
//      }}
//    </select>.toString()
//    Html(s)

