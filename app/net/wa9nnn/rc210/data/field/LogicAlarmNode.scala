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

import com.wa9nnn.wa9nnnutil.tableui.*
import controllers.routes.*
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.courtesy.CourtesyToneNode
import net.wa9nnn.rc210.data.courtesy.CourtesyToneNode.{form, keyMetadata}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.*
import net.wa9nnn.rc210.ui.nav.CheckBoxCell
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.data.Forms.*
import play.api.data.*
import play.api.i18n.MessagesProvider
import play.api.libs.json.*
import play.api.mvc.*
import play.twirl.api.Html
import views.html.{courtesyToneEdit, fieldIndex, logicAlarmEditor}

case class LogicAlarmNode(override val enabled: Boolean, lowMacro: Key, highMacro: Key) extends FieldValueComplex[LogicAlarmNode](lowMacro, highMacro) {
  lowMacro.check(KeyMetadata.Macro)
  highMacro.check(KeyMetadata.Macro)

  private val tt = Seq(
    "Enabled" -> Display(enabled),
    "Low" -> lowMacro,
    "High" -> highMacro,
  ).map(Row(_))

  override def tableSection(key: Key): TableSection =
    TableSectionButtons(key,
      Row("Low" -> lowMacro),
      Row("High" -> highMacro)
    )

  override def displayCell: Cell =
    val none: Header = Header.none
    KvTable.inACell(
      "Enabled" -> CheckBoxCell(enabled),
      "Low Macro" -> lowMacro.keyWithName,
      "Low Macro" -> highMacro.keyWithName
    )

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntry): Seq[String] = Seq {
    "todo"
  }

  override def toRow(key: Key): Row = Row(
    ButtonCell.edit(key),
    enabled,
    lowMacro,
    highMacro
  )
}

object LogicAlarmNode extends FieldDefComplex[LogicAlarmNode]:
  override val keyMetadata: KeyMetadata = KeyMetadata.LogicAlarm

  def unapply(u: LogicAlarmNode): Option[( Boolean, Key, Key)] = Some(( u.enabled, u.lowMacro, u.highMacro))

  val form: Form[LogicAlarmNode] = Form(
    mapping(
      "enable" -> boolean,
      "lowMacro" -> of[Key],
      "highMacro" -> of[Key]
    )(( enabled: Boolean, lowMacro: Key, highMacro: Key) => LogicAlarmNode.apply(enabled, lowMacro, highMacro))(LogicAlarmNode.unapply)
  )

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val enables = memory.sub8(169, 5).map(_ != 0)
    val lowMacros: Seq[Key] = memory.sub8(174, 5).map(n => Key(KeyMetadata.Macro, n + 1))
    val highMacros: Seq[Key] = memory.sub8(179, 5).map(n => Key(KeyMetadata.Macro, n + 1))
    //    SimpleField(169, "Enable", logicAlarmKey, "1n91b", FieldBoolean)
    //    SimpleField(174, "Macro Low", logicAlarmKey, "1*2101nv", MacroSelectField)
    //    SimpleField(179, "Macro High", logicAlarmKey, "1*2102nv", MacroSelectField)

    for
    {
      i <- 0 until KeyMetadata.LogicAlarm.maxN
    } yield
    {
      val key = Key(KeyMetadata.LogicAlarm, i + 1)
      val logicAlarmNode: LogicAlarmNode = LogicAlarmNode(enables(i), lowMacros(i), highMacros(i))
      FieldEntry(this, key, logicAlarmNode)
    }
  }

  implicit val fmt: OFormat[LogicAlarmNode] = Json.format[LogicAlarmNode]

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val table = Table(Header(s"Logic Alarms  (${fieldEntries.length})",
      "",
      "Logic Alarm",
      "Enable",
      "Low Macro",
      "High Macro"
    ),
      fieldEntries.map(fieldEntry => fieldEntry.value.asInstanceOf[FieldValueComplex[LogicAlarmNode]].toRow(fieldEntry.key))
    )
    fieldIndex(keyMetadata, table)

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val filled: Form[LogicAlarmNode] = form.fill(fieldEntry.value)

    logicAlarmEditor(fieldEntry.key, filled)

  override def bind(formData: FormData): Iterable[UpdateCandidate] =
    for
      key <- formData.maybeKey
    yield
      val node: LogicAlarmNode = form.bind(formData.bindable).get
      UpdateCandidate(key, node)
