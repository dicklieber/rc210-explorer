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

package net.wa9nnn.rc210.ui

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.data.datastore.DataStoreActor.UpdateData
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldKey}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.authorzation.AuthFilter.user
import org.apache.pekko.actor.typed.ActorRef
import play.api.mvc.*
import scala.concurrent.duration._

import scala.collection.immutable
import scala.collection.immutable.Seq
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.util.Timeout

import scala.concurrent.Future

/**
 * Keeps track of all the [[Key]]s for  a single [[net.wa9nnn.rc210.KeyKind]] e.g. [[net.wa9nnn.rc210.KeyKind.commonKey]] or [[net.wa9nnn.rc210.KeyKind.portKey]]; on index.
 * Then uses these, on save, to get all the values.
 * This is needed because HTML checkboxes in foems don't send any value when unchecked.
 */

/**
 *
 * @param fieldEntries that we are interested in.
 */
class SimpleValuesHandler(fieldEntries: Seq[FieldEntry]) extends LazyLogging:
  private val fieldKeys: Seq[FieldKey] = fieldEntries map (_.fieldKey)
  implicit val timeout: Timeout = 3.seconds

  /**
   * Extracts all the values from a [[Request[AnyContent]] also any named keys.
   *
   * @param request
   * @return data extracted from the [[Request]].
   */
  def collect(implicit request: Request[AnyContent]): (Seq[UpdateCandidate], Seq[NamedKey]) =
    val formDataMap: Map[FieldKey, String] = request.body.asFormUrlEncoded
      .get
      .map { (name:String, values:Seq[String]) =>
        logger.whenTraceEnabled {
          logger.trace("name: {} values: {}", name, values)
        }
        val sKey = name
        try
          val fieldKeykey: FieldKey = FieldKey(sKey)
          fieldKeykey -> values.headOption.getOrElse("")
        catch
          case e:Exception =>
            logger.error("Parsing form data", e)
            throw e
      }

    val updateCandidates: Seq[UpdateCandidate] = fieldKeys.map { fieldKey =>
      val str: String = formDataMap.getOrElse(fieldKey, "")
      logger.debug("fieldKey: {} => value: {}", fieldKey.toString, str)
      UpdateCandidate(fieldKey, Left(str))
    }
    val justNames: Map[FieldKey, String] = formDataMap.filter { (fieldKey, _) =>
      fieldKey.fieldName == "name"
    }
    val namedKeys: Seq[NamedKey] = justNames.map[NamedKey] { t =>
      val key: Key = t._1.key
      NamedKey(key, t._2)

    }.toSeq
    updateCandidates -> namedKeys

