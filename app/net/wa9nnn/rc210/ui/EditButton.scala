package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import controllers.routes
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import play.api.mvc.Call
import play.twirl.api.Html

object EditButton:
  def apply(call: Call): Html =
    Html(<a href={call.url} class="bi bi-pencil-square btn p-0"></a>
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

object  EditFlowButtons:
  def apply(fieldKey: FieldKey):Html =
    val htmlEditButton: Html = EditButton(fieldKey)

    val flowCall = routes.FlowController.flowChart(fieldKey.key).url
    val flowHtmlButton = Html(<a href={flowCall} class="bi">&#10199;</a>.toString)
    val both: String = htmlEditButton.body + flowHtmlButton.body
    Html(both)
    
  def cell(fieldKey: FieldKey):Cell =
    Cell.rawHtml(apply(fieldKey).body)
