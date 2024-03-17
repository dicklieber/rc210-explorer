package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.FieldKey
import play.twirl.api.Html

import scala.compiletime.ops.string

object RollbackButton:
  def apply(maybeFieldKey: Option[FieldKey] = None): Html =
    val (call, toolTip) = maybeFieldKey match
      case Some(fieldKey) =>
        controllers.routes.DataStoreController.rollbackOne(fieldKey) -> s"""Rollback only: "${fieldKey.display}"; remove candidate."""
      case None =>
        controllers.routes.DataStoreController.rollback() -> s"Rollback all candidates."

    val string: String = <a href={call.url} class="bi bi-arrow-counterclockwise btn p-0" title={toolTip}></a>.toString
    Html(string)

  def cell(): Cell =
    val html: Html = apply(None)
    Cell.rawHtml(html.body)

  def cell(fieldKey: FieldKey): Cell =
    val html = apply(Option(fieldKey))
    Cell.rawHtml(html.body)


