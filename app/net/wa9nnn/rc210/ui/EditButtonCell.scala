package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import play.api.mvc.Call
import play.twirl.api.Html
import views.html.editButton

object EditButtonCell:
  def apply(call: Call): Cell =
    val html: Html = editButton(call)
    Cell.rawHtml(html.toString)

object EditButton:
  def apply(fieldKey: FieldKey): Html =
    val call = controllers.routes.EditController.edit(fieldKey)
    val html: Html = editButton(call)
    html


