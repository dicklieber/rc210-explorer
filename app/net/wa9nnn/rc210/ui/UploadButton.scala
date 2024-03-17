package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.*
import net.wa9nnn.rc210.serial.UploadRequest
import play.api.libs.json.Json
import play.api.mvc.Call
import play.twirl.api.Html

object UploadButton:
  def apply(uploadRequest: UploadRequest): Html =
    val toolTip = Json.prettyPrint(Json.toJson( uploadRequest))
    val call = controllers.routes.UploadController.start(uploadRequest)
    Html(<a href={call.url} class="bi bi-upload btn btn-sm p-0"  title={toolTip}></a>.toString)


  def cell(uploadRequest: UploadRequest): Cell =
    Cell.rawHtml(apply(uploadRequest).body)

