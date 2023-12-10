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

package net.wa9nnn.rc210.data.datastore

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey, FieldValue}
import play.api.mvc.Result
import play.api.mvc.Results.*

import scala.util.{Failure, Success, Try}

/**
 * What a [[DataStoreRequest]] returns from the [[DataStoreActor]]
 *
 * @param tried from  [[DataStoreLogic]]
 */
case class DataStoreReply(tried: Try[Seq[FieldEntry]]) extends LazyLogging {
  def forHead[T <: FieldValue](f: (FieldKey, T) => Result): Result = {
    tried match
      case Failure(exception) =>
        logger.error("DataStoreReply", exception)
        InternalServerError(exception.getMessage)
      case Success(fieldEntries: Seq[FieldEntry]) =>
        fieldEntries.headOption.map { fe =>
          f(fe.fieldKey, fe.value.asInstanceOf[T])
        }.getOrElse(throw new IllegalStateException("No FieldEntry returned!"))
  }
}



