package net.wa9nnn.rc210.data

import com.wa9nnn.wa9nnnutil.tableui.Table
import controllers.routes
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldValue}
import net.wa9nnn.rc210.ui.{AbstractTab, SimpleValuesHandler, Tabs}
import play.api.data.Form
import play.api.i18n.MessagesProvider
import play.api.mvc.{Request, RequestHeader, Result, Results}

trait EditHandler[T <: FieldValue]:
  //  var svh: Option[SimpleValuesHandler] = None
  val keyKind: KeyKind

  def form: Form[T]

  def index(values: Seq[T]): Table

  /**
   * Invoked by [[controllers.EditController]].
   *
   * @param value from datastore
   * @return
   */
  final def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Result =

    //    if (svh.isEmpty)
    //      svh = Some(new SimpleValuesHandler(fieldEntries))

    val value: T = fieldEntry.value
    editOp(form.fill(value), fieldEntry.fieldKey)

  def editOp(form: Form[T], fieldKey: FieldKey)(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result

  def bindFromRequest(data: Map[String, Seq[String]]): ComplexFieldValue

  //  def saveOkResult(): Result
  def saveOp()(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result =
    Results.Redirect(routes.EditController.index(keyKind))
//    Results.ImATeapot("todo EditHandler")
//    val r: Result = form.bindFromRequest()
//      .fold(
//        (formWithErrors: Form[T]) => {
//          BadRequest(editOp(formWithErrors))
//          BadRequest(views.html.clock(formWithErrors))
//        },
//        (clock: ClockNode) => {
//          val candidateAndNames = ProcessResult(clock)
//
//          given RcSession = request.attrs(sessionKey)
//
//          dataStore.update(candidateAndNames)
//          Redirect(routes.ClockController.index)
//        }
//      )
//    r

