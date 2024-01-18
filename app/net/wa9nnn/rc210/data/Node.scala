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

import com.wa9nnn.wa9nnnutil.tableui.{Table, TableSection}
import net.wa9nnn.rc210.{FieldKey, Key}
import net.wa9nnn.rc210.ui.flow.D3Node

trait Node:
  val key: Key

  //  def table(fieldKey: FieldKey, includeMacroKey:Boolean = false): Table =
  def table(fieldKey: FieldKey): Table =
    Table.empty(s"todo: $key")

  def d3Node(nodeKey: String): D3Node =
    D3Node(nodeKey, toString, "todo", "todo")

/**
 * A [[Node]] that can invoke a Macro
 */
trait TriggerNode(val macroKeys: Key*) extends Node:
  def enabled: Boolean = true

  final def canRunMacro(candidate: Key): Boolean =
    enabled && macroKeys.contains(candidate)

  def tableSection(fieldKey: FieldKey): TableSection

  def triggerInfo(fieldKey: FieldKey, tableSection: TableSection): TriggerInfo =
    TriggerInfo(fieldKey, this)

case class TriggerInfo(fieldKey: FieldKey, triggerNode: TriggerNode ):
  def tableSection: TableSection = triggerNode.tableSection(fieldKey)
  def canRun(key:Key):Boolean =
    triggerNode.canRunMacro(key)

