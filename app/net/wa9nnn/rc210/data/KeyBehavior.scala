package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.data.field.FieldValue
import play.api.data.Form
import play.api.mvc.Result


/**
 * What the [[EditorContainer]] does for a [[net.wa9nnn.rc210.KeyKind]]
 * @tparam T
 */
trait KeyBehavior[T <: FieldValue] {
  def form(): Form[T]

  def index(): Result

  def edit(fieldKey: FieldKey): Result

  def save():Unit
}

