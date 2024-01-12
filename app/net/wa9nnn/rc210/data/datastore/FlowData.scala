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
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.TriggerNode
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.functions.FunctionsProvider
import net.wa9nnn.rc210.data.macros.MacroNode

import scala.language.postfixOps

/**
 *
 * @param rcMacro  that this flow centers around.
 * @param triggers what this macro does.
 * @param searched what we looked for. UI should highlight this node. 
 */
case class FlowData(rcMacro: MacroNode, triggers: Seq[FieldEntry], searched: Key):
  def table(functionsProvider: FunctionsProvider): Table =
    KvTable.apply("Flow Data",
      "Search" -> searched.keyWithName,
      "Macro" -> rcMacro.key.keyWithName,
      TableSection("Triggers", triggers.map { fieldEntry =>
        val value: TriggerNode = fieldEntry.value.asInstanceOf[TriggerNode]
        Row.ofAny(fieldEntry.fieldKey, value)
      }),
      TableSection("Functions", rcMacro.functions.map { functionKey =>
        val description = functionsProvider(functionKey).map{ fn =>
          fn.description
        }.getOrElse(functionKey.toString)
        Row.ofAny(functionKey.keyWithName, description)
      })
    )






