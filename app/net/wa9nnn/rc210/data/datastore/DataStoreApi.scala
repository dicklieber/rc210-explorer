/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */

package net.wa9nnn.rc210.data.datastore

import net.wa9nnn.rc210.{Key, KeyMetadata}
import net.wa9nnn.rc210.data.field.FieldEntry

/**
 * The `DataStoreApi` trait represents an interface for managing and retrieving data entries 
 * in a structured data store. It provides methods to access, manipulate, and retrieve field 
 * entries, manage candidates, and handle rollbacks.
 * Note that persistence is not handled by this trait.
 */
trait DataStoreApi:
  def fieldEntry(Key: Key): FieldEntry
  def fieldEntry(key: KeyMetadata): Seq[FieldEntry]
  def candidates: Iterable[FieldEntry]
  def triggerNodes(macroKey: Key): Seq[FieldEntry]
  def update(candidateAndNames: CandidateAndNames): Unit
  def acceptCandidate(Key: Key): Unit
  def rollback(): Unit
  def rollback(Key: Key): Unit
  def flowData(search: Key): Option[FlowData]