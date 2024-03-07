package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import play.api.mvc.Call
import play.twirl.api.Html

object EditButton:
  def apply(call: Call): Html =
    Html(<a href={call.url} class="bi bi-pencil-square btn p-0" onclick="window.location.href='{call.url}"></a>
//    Html(<button type="button" class="bi bi-pencil-square btn p-0" onclick="window.location.href='{call.url}">
//    </button>
      .toString
    )

  def cell(fieldKey: FieldKey): Cell =
    Cell.rawHtml(apply(fieldKey).body)


  def apply(fieldKey: FieldKey): Html =
    val call = controllers.routes.EditController.edit(fieldKey)
    val html: Html = apply(call)
    html


