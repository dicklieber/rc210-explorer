package net.wa9nnn.rc210.data

import com.wa9nnn.wa9nnnutil.tableui.Table
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.ui.{AbstractTab, Tabs}
import play.api.data.Form
import play.api.i18n.MessagesProvider
import play.api.mvc.{RequestHeader, Result}

trait EditHandler[T <: FieldValue]:
  def form: Form[T]

  def index(values: Seq[T]): Table

  /**
   * Invoked by [[controllers.EditController]].
   *
   * @param value from datastore
   * @return
   */
  final def edit( fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Result =
    val value: T = fieldEntry.value
    editOp(form.fill(value), fieldEntry.fieldKey)

  
  def editOp(form: Form[T], fieldKey: FieldKey)(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result

//  def saveOkResult(): Result


