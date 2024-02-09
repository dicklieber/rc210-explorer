package net.wa9nnn.rc210.data

import com.wa9nnn.wa9nnnutil.tableui.Table
import controllers.routes
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldValue}
import net.wa9nnn.rc210.ui.{SimpleValuesHandler, Tab, Tabs}
import play.api.data.Form
import play.api.i18n.MessagesProvider
import play.api.mvc.{Call, Request, RequestHeader, Result, Results}
import play.twirl.api.Html
import views.html.helper.form
import play.api.i18n.MessagesProvider

trait EditHandler[T <: FieldValue]:
  /**
   * Allows overriding where  [[controllers.EditController]] will go for index request.
   *
   * @param keyKind
   * @return
   */
  def redirect(keyKind: KeyKind): Option[Call] = None

  //  var svh: Option[SimpleValuesHandler] = None
  val keyKind: KeyKind

  //  def form: Form[T]

  def index(values: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html

  /**
   * Invoked by [[controllers.EditController]].
   *
   * @param value from datastore
   * @return
   */
  def edit(fieldEntry:FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html
  //   def save(data: Map[String, Seq[String]])(using request: RequestHeader, messagesProvider: MessagesProvider): Result

  //  def editOp(fieldEntry:FieldEntry)(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result

  def bindFromRequest(data: Map[String, Seq[String]]): Seq[UpdateCandidate]

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

