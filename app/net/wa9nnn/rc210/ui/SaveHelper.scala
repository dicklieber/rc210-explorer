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

import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldKey}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*
import play.api.mvc.{MessagesAbstractController, *}

/**
 * Handle save for any [[ComplexFieldValue]] in a play Controller
 *
 */
abstract class ComplexFieldController[T <: ComplexFieldValue](
                                                               form: Form[T], 
                                                               indexResult: (values:Seq[T]) => Result, 
                                                               editResult: (form:Form[T], namedKey:NamedKey) =>Result,
                                                               saveOkResult: () => Result
                                                             )
                                                             (dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) {

  def index(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val valueT = form.get
      val key = valueT.key
      
      val logicAlarms: Seq[T] = dataStore.values(key.keyKind)
      indexResult(logicAlarms)
  }

  def edit(key: Key): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey(key)
      val value: T = dataStore.editValue(fieldKey)
      val form: Form[T] = form.fill(value)
     editResult(form, key.namedKey)
  }

  /**
   *
   * @param form      for play form for a T.
   * @param ok        form had no errors, where should, Usually a play Redirect.
   * @param error     form has errors, redisplay with
   * @param request   the HTTP request.
   * @param dataStore where we put stuff
   * @tparam T
   */
  def save() = Action[AnyContent]  {
    implicit request: MessagesRequest[AnyContent] =>
    form.bindFromRequest(request.body.asFormUrlEncoded.get)
      .fold(
        (formWithErrors: Form[T]) => {
          val namedKey = Key(formWithErrors.data("key")).namedKey
          error(formWithErrors, namedKey)
        },
        (success: T) => {

          val candidateAndNames = ProcessResult(success)

          given RcSession = request.attrs(sessionKey)

          dataStore.update(candidateAndNames)
          ok
        }
      )

}
