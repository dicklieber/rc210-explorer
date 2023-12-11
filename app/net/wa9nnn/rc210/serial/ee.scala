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

package net.wa9nnn.rc210.serial


import java.time.{Duration, Instant}

object LastSendBatch:
  var maybeLastSendBatch: Option[LastSendBatch] = None

/**
 * About the last [[BatchRc210Sender]] operation.
 *
 * @param operations what we did
 * @param start      when it started
 * @param finish     when it finshed
 */
case class LastSendBatch(operations: Seq[BatchOperationsResult], start: Instant, finish: Instant = Instant.now()):
  val duration: Duration = Duration.between(start, finish)
  var successCount: Int = 0
  var failCount: Int = 0

  for {
    bo: BatchOperationsResult <- operations
    op: RcOperationResult <- bo.results
  } {
    if (op.isSuccess)
      successCount += 1
    else
      failCount += 1
  }