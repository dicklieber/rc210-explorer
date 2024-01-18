/*
 * Copyright (C) 2024  Dick Lieber, WA9NNN                               
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

package net.wa9nnn.rc210.data.datastore

import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.{FieldKey, Key}
import net.wa9nnn.rc210.data.{Node, TriggerInfo, TriggerNode}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.ui.flow.{D3Data, D3Link, D3Node}

import scala.language.postfixOps

/**
 *
 * @param Macro  that this flow centers around.
 * @param triggers what this macro does.
 * @param searched what we looked for. UI should highlight this node. 
 */
case class FlowData(macroFieldEntry: FieldEntry, triggers: Seq[TriggerInfo], searched: Key):
  private val macroNode: MacroNode = macroFieldEntry.value
  private val macroFieldKey: FieldKey = macroFieldEntry.fieldKey

  def triggersTable:Table =
    var table: Table = KvTable("Triggers")
    triggers.foreach{triggerInfo =>
      table = table.appendSection(triggerInfo.tableSection)
    }
    table
  
  def functionsTable: Table =
    val f: Seq[(String, String)] = macroNode.functions.map { functionKey =>
      functionKey.rc210Value.toString -> FunctionsProvider(functionKey).description
    }

    KvTable(f: _*)

  def macroSection: Table =
    KvTable(s"Macro ${macroFieldEntry.fieldKey.key.keyWithName}",
      "DTMF" -> macroFieldEntry.value[MacroNode].dtmf
    )

  def table: Table = {
    val triggerRows: Seq[Row] = triggers.map { triggerInfo =>
//      val value: TriggerNode = triggerInfo.tableSection
      Row.ofAny(triggerInfo.fieldKey, triggerInfo.tableSection)
    }
    val functionRows = macroNode.functions.map { functionKey =>
      val description = FunctionsProvider(functionKey).description
      Row.ofAny(functionKey.keyWithName, description)
    }
    KvTable.apply("Flow Data",
      "Search" -> searched.keyWithName,
      "Macro" -> macroFieldKey.key.keyWithName,
      TableSection("Triggers", triggerRows: _*),
      TableSection("Functions", functionRows: _*)
    )
  }

  def d3Data(): D3Data = ???

/*
    val fNodes: Seq[D3Node] = macroNode.functions.map(functionKey => FunctionsProvider(functionKey).d3Node(functionKey.toString))
    val tNodes: Seq[D3Node] = triggers.map { fe =>
      val n: TriggerNode = fe.value
      n.d3Node(fe.fieldKey.toString)
    }
    val nodes: Seq[D3Node] = (fNodes ++ tNodes).+:(macroNode.d3Node(macroFieldKey.toString))

    val linkBuilder = Seq.newBuilder[D3Link]
    triggers.foreach { tn =>
      linkBuilder += D3Link(macroFieldKey.toString, tn.fieldKey.toString)
    }

    D3Data(
      nodes = nodes
      ,
      Seq.empty
    )
*/






