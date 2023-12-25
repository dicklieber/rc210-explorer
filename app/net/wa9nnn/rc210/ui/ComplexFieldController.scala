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

import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue}
import net.wa9nnn.rc210.data.named.NamedKey
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.{FieldKey, Key}
import play.api.data.Form
import play.api.mvc.*

/**
 * Handle logic for any [[ComplexFieldValue]]  play Controller.
 *
 * @param dataStore where RC-210 data lives.
 * @param components needed for any Controller.
 * @tparam T
 */
abstract class ComplexFieldController[T <: ComplexFieldValue](dataStore: DataStore, components: MessagesControllerComponents)
  extends MessagesAbstractController(components) {
  val complexExtractor: ComplexExtractor[T]

  /**
   * Build the Index page for all T for the [[net.wa9nnn.rc210.KeyKind]]
   *
   * @param values current i.e value or candidate for each instance. For example:
   * {{{
   *      override def indexResult(values: Seq[LogicAlarm]): Result =
   *        Ok(views.html.logic(values))
   * }}}
   * @return
   */
  def indexResult(values: Seq[T]): Result

  /**
   * Build the page to edit the T for the Key
   *
   * for the selected T.
   * For example:
   * {{{
   *      override def editResult(filledForm: Form[LogicAlarm], namedKey: NamedKey)(using request: MessagesRequest[AnyContent]): Result =
   *        Ok(views.html.logicEditor(filledForm, namedKey))
   *
   * }}}
   *
   * @param filledForm play [[Form]]
   * @param namedKey   to allow editing the key name.
   * @return
   */
  def editResult(filledForm: Form[T], namedKey: NamedKey)(using request: MessagesRequest[AnyContent]): Result

  /**
   * Build the page to return to after a successfull save.
   *
   * @param values current i.e value or candidate for each instance. For example:
   * {{{
   *      override def saveOkResult(): Result =
   *        Redirect(routes.LogicAlarmEditorController.index)
   *
   * }}}
   * @return
   */
  def saveOkResult(): Result

  def index: Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val keyKind = complexExtractor.keyKind
      val values: Seq[T] = dataStore.values(keyKind)
      indexResult(values)
  }

  def edit(key: Key): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val fieldKey = FieldKey(key)
      val value: T = dataStore.editValue(fieldKey)
      val filledForm: Form[T] = complexExtractor.form.fill(value)
      editResult(filledForm, key.namedKey)(using request)
  }

  def save(): Action[AnyContent] = Action {
    implicit request: MessagesRequest[AnyContent] =>
      val map = request.body.asFormUrlEncoded.get
      val r: Result = complexExtractor.form.bindFromRequest(map)
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