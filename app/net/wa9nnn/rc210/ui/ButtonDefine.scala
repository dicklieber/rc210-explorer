package net.wa9nnn.rc210.ui

import controllers.routes
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.serial.UploadRequest
import net.wa9nnn.rc210.ui.Button.button
import play.api.mvc.Call

/**
 * Create the html for variopus buttons.
 */
object Button:

  /**
   *
   * @param icon    a bootstrap icon name e.g. "bi-upload"  @See [[https://icons.getbootstrap.com]]
   * @param call    from Play routes: e.g. [[controllers.routes.UploadController.start(uploadRequest)]].
   * @param tooltip html title attribute.
   * @return raw html.
   */
  def button(icon: String, call: Call, toolTip: String): String =
    val cssClass = s"bi {icon}} btn ${icon} btn-sm p-0"
    <a href={call.url} class={cssClass} title={toolTip}></a>
      .toString

  def flow(fieldKey: FieldKey): String =
    button(
      icon = "bi-link-45deg",
      call = routes.FlowController.flowChart(fieldKey.key),
      toolTip = "Show in flow page.")

  val editIcon = "bi-pencil-square"

  def edit(fieldKey: FieldKey): String = {
    button(
      icon = editIcon,
      call = routes.EditController.edit(fieldKey),
      toolTip = "Edit this field.")
  }

  def rollback(): String =
    buildRollback(None)

  def rollback(fieldKey: FieldKey): String =
    buildRollback(Option(fieldKey))

  def buildRollback(fieldKey: Option[FieldKey]): String =
    val icon = "bi-arrow-counterclockwise"
    fieldKey match
      case Some(fieldKey) =>
        button(icon,
          routes.DataStoreController.rollbackOne(fieldKey),
          s"Rollback ${fieldKey.display}. Remove candidate restore original value.")
      case None =>
        button(icon,
          routes.DataStoreController.rollback(),
          "Rollback all fields.")

  def upload(doCandidateOnly:Boolean = true): String =
    buildUpload(None, doCandidateOnly)

  def upload(fieldKey: FieldKey): String =
    buildUpload(Option(fieldKey))

  def buildUpload(fieldKey: Option[FieldKey], doCandidateOnly:Boolean = true): String =
    val icon = "bi-upload"
    fieldKey match
      case Some(fieldKey) =>
        button(icon,
          routes.UploadController.start(UploadRequest(fieldKey)),
          s"Upload ${fieldKey.display} and accept the candidate.")
      case None =>
        button(icon,
          routes.UploadController.start(UploadRequest(doCandidateOnly = doCandidateOnly)),
          s"Upload all fields")
