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
import controllers.routes
import controllers.routes.*
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.courtesy.CourtesyToneNode
import net.wa9nnn.rc210.data.courtesy.CourtesyToneNode.{form, keyKind}
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

case class LogicAlarmNode(override val key: Key, override val enabled: Boolean, 
                          lowMacro: Key, highMacro: Key) extends FieldValueComplex[LogicAlarmNode](lowMacro, highMacro) {
  key.check(KeyMetadata.LogicAlarm)
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
      "Key" -> key.keyWithName,
      "Enabled" -> CheckBoxCell(enabled),
      "Low Macro" -> lowMacro.keyWithName,
      "Low Macro" -> highMacro.keyWithName
    )

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: TemplateSource): Seq[String] = Seq {
    "todo"
  }

  override def toJsValue: JsValue = Json.toJson(this)

  override def toRow: Row = Row(
    ButtonCell.edit(fieldKey),
    key.keyWithName,
    enabled,
    lowMacro,
    highMacro
  )
}

object LogicAlarmNode extends FieldDefComplex[LogicAlarmNode]:
  override val keyKind: KeyMetadata = KeyMetadata.LogicAlarm

  def unapply(u: LogicAlarmNode): Option[(Key, Boolean, Key, Key)] = Some((u.key, u.enabled, u.lowMacro, u.highMacro))

  val form: Form[LogicAlarmNode] = Form(
    mapping(
      "key" -> of[Key],
      "enable" -> boolean,
      "lowMacro" -> of[Key],
      "highMacro" -> of[Key]
    )(LogicAlarmNode.apply)(LogicAlarmNode.unapply)
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
      val logicAlarmKey = Key(KeyMetadata.LogicAlarm, i + 1)
      val logicAlarmNode: LogicAlarmNode = new LogicAlarmNode(logicAlarmKey, enables(i), lowMacros(i), highMacros(i))
      FieldEntry(this, logicAlarmNode.fieldKey, logicAlarmNode)
    }
  }

  override def positions: Seq[FieldOffset] = Seq()

  implicit val fmt: OFormat[LogicAlarmNode] = Json.format[LogicAlarmNode]

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val table = Table(Header(s"Logic Alarms  (${fieldEntries.length})",
      "",
      "Logic Alarm",
      "Enable",
      "Low Macro",
      "High Macro"
    ),
      fieldEntries.map(_.value.toRow)
    )
    fieldIndex(keyKind, table)

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val filled: Form[LogicAlarmNode] = form.fill(fieldEntry.value)

    logicAlarmEditor(filled, fieldEntry.fieldKey)

  override def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate] =
    val courtesyTone = form.bindFromRequest(data).get
    Seq(
      UpdateCandidate(candidate = courtesyTone)
    )

