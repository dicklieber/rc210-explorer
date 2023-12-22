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

import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.Who
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*
import Who.request2Session
/**
 * Handle save for any [[ComplexFieldValue]] in a play Controller
 *
 */
abstract class ComplexFieldController[T <: ComplexFieldValue]( dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) {
  val form: Form[T]
  val complexExtractor: ComplexExtractor

  def indexResult(values: Seq[T]): Result

  def saveOkResult():  Result

  def editResult(filledForm: Form[T], namedKey: NamedKey)(using request:MessagesRequest[AnyContent]): Result



  def index(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val keyKind = complexExtractor.keyKind
      val values: Seq[T] = dataStore.values(keyKind)
      indexResult(values)
  }

  def edit(key: Key): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey(key)
      val value: T = dataStore.editValue(fieldKey)
      val filledForm: Form[T] = form.fill(value)
      editResult(filledForm, key.namedKey)(using request)
  }

  def save(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val r: Result = form.bindFromRequest(request.body.asFormUrlEncoded.get)
        .fold(
          (formWithErrors: Form[T]) => {
            val namedKey = Key(formWithErrors.data("key")).namedKey

            editResult(formWithErrors, namedKey)
          },
          (success: T) => {

            val candidateAndNames = ProcessResult(success)

            given RcSession = request.attrs(sessionKey)

            dataStore.update(candidateAndNames)
            val result: Result = saveOkResult()
            result
          }
        )
      r

  }
}