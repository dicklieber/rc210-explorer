package net.wa9nnn.rc210.data

import controllers.routes
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry, FieldValue}
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import play.api.i18n.MessagesProvider
import play.api.mvc.{Call, RequestHeader, Result, Results}
import play.twirl.api.Html

/**
 * This defined the behavior for a [[KeyKind]].
 * @tparam T
 */
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
  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html
  //   def save(data: Map[String, Seq[String]])(using request: RequestHeader, messagesProvider: MessagesProvider): Result

  //  def editOp(fieldEntry:FieldEntry)(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result

  def bindFromRequest(data: Map[String, Seq[String]]): Seq[UpdateCandidate]

  def bindOne(in: ComplexFieldValue): Seq[UpdateCandidate] =
    Seq(
      UpdateCandidate(in.fieldKey, in)
    )
  def bindOne(in:String, fieldKey: FieldKey): Seq[UpdateCandidate] =
    Seq(
      UpdateCandidate(fieldKey, in)
    )

  //  def saveOkResult(): Result
  def saveOp()(implicit request: RequestHeader, messagesProvider: MessagesProvider): Result =
    Results.Redirect(routes.EditController.index(keyKind))

