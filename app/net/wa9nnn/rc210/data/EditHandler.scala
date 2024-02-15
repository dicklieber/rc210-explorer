package net.wa9nnn.rc210.data

import controllers.routes
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldEntry}
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import play.api.i18n.MessagesProvider
import play.api.mvc.{RequestHeader, Result, Results}
import play.twirl.api.Html

/**
 * Defines the behavior for a specific [[KeyKind]].
 */
trait EditHandler:

  val keyKind: KeyKind

  /**
   * The landing page for a [[KeyKind]]. This may have the edit behavior from some [[KeyKind]]s.
   * @param fieldEntries from [[net.wa9nnn.rc210.data.datastore.DataStore]].
   * @return
   */
  def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html

  /**
   * Invoked by [[controllers.EditController]].
   *
   * @param fieldEntry from the [[net.wa9nnn.rc210.data.datastore.DataStore]].
   * @return
   */
  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html

  def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate]

  def bind(in: ComplexFieldValue): Seq[UpdateCandidate] =
    Seq(
      UpdateCandidate(in.fieldKey, in)
    )

  def bind(in: String, fieldKey: FieldKey): Seq[UpdateCandidate] =
    Seq(
      UpdateCandidate(fieldKey, in)
    )

  // todo May not need this in EditHandler if default is always used.
  def saveOp(): Result =
    Results.Redirect(routes.EditController.index(keyKind))

/**
 * Helpers to pick apart form form data.
 */
object EditHandler:
  def str(name: String)(implicit data: Map[String, Seq[String]]): Option[String] =
    for {
      s: Seq[String] <- data.get(name)
      h <- s.headOption
    } yield {
      h
    }

  def fieldKey(using data: Map[String, Seq[String]]): Option[FieldKey] =
    str("fieldKey")(data).map(FieldKey(_))

