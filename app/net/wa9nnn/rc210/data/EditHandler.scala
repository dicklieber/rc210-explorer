package net.wa9nnn.rc210.data

import com.wa9nnn.wa9nnnutil.tableui.Cell
import controllers.routes
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValueComplex}
import net.wa9nnn.rc210.ui.FormData
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.i18n.MessagesProvider
import play.api.mvc.{RequestHeader, Result, Results}
import play.twirl.api.Html

/**
 * Defines the behavior for a specific [[KeyMetadata]].
 */
trait EditHandler:

  /**
   * The landing page for a [[KeyMetadata]]. This may have the edit behavior from some [[KeyMetadata]]s.
   *
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

  def bind(formData: FormData): Seq[UpdateCandidate] =
    throw new NotImplementedError()

  def bind(key:Key, in: FieldValueComplex[?]): Seq[UpdateCandidate] =
    Seq(
      UpdateCandidate(key, candidate = in)
    )

  def bind(in: String, key: Key): Seq[UpdateCandidate] =
    Seq(
      UpdateCandidate(key, candidate = in)
    )

  // todo May not need this in EditHandler if default is always used.
  def saveOp(keyMetadata: KeyMetadata): Result =
    Results.Redirect(routes.EditController.index(keyMetadata))

/**
 * Helpers to pick apart form form data.
 */
object EditHandler:
  def str(name: String)(implicit data: Map[String, Seq[String]]): Option[String] =
    for
    {
      s: Seq[String] <- data.get(name)
      h <- s.headOption
    } yield
    {
      h
    }

  def fieldKey(using data: Map[String, Seq[String]]): Option[Key] = {
    val fieldKeyName: Option[String] = str(Key.keyName)

    fieldKeyName.map { s =>
      Key.fromId(s)
    }
  }

