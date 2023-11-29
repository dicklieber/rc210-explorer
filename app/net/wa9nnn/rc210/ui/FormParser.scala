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
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.ComplexFieldValue
import net.wa9nnn.rc210.data.named.NamedKey
import play.api.mvc.*


object FormParser:
  /**
   * Parses [[ComplexFieldValue]]s from <form> data.
   *
   * @param request from Browser
   * @return data to send to the DataStore
   */
  def apply(formParseable: FormParseable)(implicit request: Request[AnyContent]): CandidateAndNames =
    val x = new InternalFormParser(formParseable, request)
    x.result

  private class InternalFormParser(formParseable: FormParseable, request: Request[AnyContent]) {

    //    // A map of all the form values from an HTML form.
    //    // Note checkboxes send no value if unchecked; you have to know what you're looking for.
    //    val data: Map[String, String] = request.body.asFormUrlEncoded.get.flatMap { (name, values: Seq[String]) =>
    //      values.headOption.map(name -> _)
    //    }
    private val formFields: FormFields = FormExtractor(request)

    private val fieldValue: ComplexFieldValue = formParseable.parseForm(formFields)
    private val namedKeys: Seq[NamedKey] = (for {
      name: String <- formFields.stringOpt("name")
      key: Key <- formFields.key
    } yield {
      NamedKey(key, name)
    }).iterator.toSeq

    def result: CandidateAndNames =
      CandidateAndNames(Seq(UpdateCandidate(fieldValue)), namedKeys)
  }

class FormExtractor(request: Request[AnyContent]) extends FormFields with LazyLogging:
  // A map of all the form values from an HTML form.
  // Note checkboxes send no value if unchecked; you have to know what you're looking for.
  val data: Map[String, String] = request.body.asFormUrlEncoded.get.flatMap { (name, values: Seq[String]) =>
    values.headOption.map(name -> _)
  }
  logger.whenDebugEnabled {
    data.foreach { t2 =>
      logger.debug(t2.toString())
    }
  }


trait FormFields:
  val data: Map[String, String]

  def key: Option[Key] = key("key")


  def key(keyName: String): Option[Key] = data.get(keyName).map(Key(_))

  def int(name: String): Int =
    string(name).toInt

  def stringOpt(name: String): Option[String] =
    data.get(name)

  def string(name: String): String =
    data(name)

  def boolean(name: String): Boolean = {
    data.contains(name)
  }

trait FormParseable:
  def parseForm(formFields: FormFields): ComplexFieldValue


case class CandidateAndNames(candidates: Seq[UpdateCandidate], namedKeys: Seq[NamedKey] = Seq.empty)