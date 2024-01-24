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
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.*
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Forms.*
import play.api.data.*
import play.api.i18n.MessagesProvider
import play.api.libs.json.*
import play.api.mvc.*
import views.html.{editButton, logicAlarmEditor}

case class LogicAlarmNode(override val key: Key, override val enabled: Boolean, lowMacro: Key, highMacro: Key) extends ComplexFieldValue(lowMacro, highMacro) {
  key.check(KeyKind.LogicAlarm)
  lowMacro.check(KeyKind.Macro)
  highMacro.check(KeyKind.Macro)

  private val tt = Seq(
    "Enabled" -> Display(enabled),
    "Low" -> lowMacro,
    "High" -> highMacro,
  ).map(Row(_))

  override def tableSection(fieldKey: FieldKey): TableSection =
    TableSectionButtons(fieldKey, routes.EditController.edit(fieldKey),
      "Low" -> lowMacro, "High" -> highMacro)

  override def displayHtml: String =
    <table>
      <tr>
        <td>Enabled</td>
        <td>
          {Display(enabled)}
        </td>
      </tr>
      <tr>
        <td>Low</td>
        <td>
          {lowMacro}
        </td>
      </tr>
      <tr>
        <td>High</td>
        <td>
          {highMacro}
        </td>
      </tr>
    </table>
      .toString

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = Seq {
    "todo"
  }

  override def toJsValue: JsValue = Json.toJson(this)

  override def toRow: Row = Row(EditButtonCell(fieldKey),
    enabled,
    lowMacro,
    highMacro
  )
}

object LogicAlarmNode extends ComplexExtractor[LogicAlarmNode] with EditHandler[LogicAlarmNode] {
  override val keyKind: KeyKind = KeyKind.LogicAlarm

  override def editOp(form: Form[LogicAlarmNode], fieldKey: FieldKey)(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result =
    Results.Ok(logicAlarmEditor(form, fieldKey))

//  override def editOp(form: Form[LogicAlarmNode], fieldKey: FieldKey)(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result =
//    Results.Ok(logicAlarmEditor(form, fieldKey))

  def unapply(u: LogicAlarmNode): Option[(Key, Boolean, Key, Key)] = Some((u.key, u.enabled, u.lowMacro, u.highMacro))

  val form: Form[LogicAlarmNode] = Form(
    mapping(
      "key" -> of[Key],
      "enable" -> boolean,
      "lowMacro" -> of[Key],
      "highMacro" -> of[Key]
    )(LogicAlarmNode.apply)(LogicAlarmNode.unapply)
  )

  override def index(values: Seq[LogicAlarmNode]): Table =
    Table(Header(s"Logic Alarm  (${values.length})",
      "Logic Alarm",
      "Enable",
      "Low Macro",
      "High Macro"
    ),
      values.map(_.toRow)
    )

  /**
   *
   * @param memory    source of RC-210 data.
   * @return what we extracted.
   */
  override def extract(memory: Memory): Seq[FieldEntry] = {
    val enables = memory.sub8(169, 5).map(_ != 0)
    val lowMacros: Seq[Key] = memory.sub8(174, 5).map(n => Key(KeyKind.Macro, n + 1))
    val highMacros: Seq[Key] = memory.sub8(179, 5).map(n => Key(KeyKind.Macro, n + 1))
    //    SimpleField(169, "Enable", logicAlarmKey, "1n91b", FieldBoolean)
    //    SimpleField(174, "Macro Low", logicAlarmKey, "1*2101nv", MacroSelectField)
    //    SimpleField(179, "Macro High", logicAlarmKey, "1*2102nv", MacroSelectField)

    for {
      i <- 0 until KeyKind.LogicAlarm.maxN
    } yield {
      val logicAlarmKey = Key(KeyKind.LogicAlarm, i + 1)
      val fieldValue: LogicAlarmNode = new LogicAlarmNode(logicAlarmKey, enables(i), lowMacros(i), highMacros(i))
      FieldEntry(this, fieldValue)
    }
  }

  override def parse(jsValue: JsValue): LogicAlarmNode = jsValue.as[LogicAlarmNode]

  /**
   * for various things e.g. parser name.
   */
  override val name: String = "LogicAlarm"
  override val fieldName: String = name

  override def positions: Seq[FieldOffset] = Seq()

  implicit val fmtLogicAlarm: Format[LogicAlarmNode] = Json.format[LogicAlarmNode]


  
//  override def editOp(form: Form[LogicAlarmNode], fieldKey: FieldKey): Result =
//    logicAlarmEditor(form, fieldKey)

  override def bindFromRequest(data: Map[String, Seq[String]]): LogicAlarmNode = {
    val value: Form[LogicAlarmNode] = form.bindFromRequest(data)
    val hasErrors: Boolean = value.hasErrors
    val errors: Seq[FormError] = value.errors
    val mayBeValue: Option[LogicAlarmNode] = value.value
    value.get
  }
}
