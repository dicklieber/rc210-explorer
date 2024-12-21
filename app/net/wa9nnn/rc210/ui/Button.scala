package net.wa9nnn.rc210.ui

import controllers.routes
import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.KeyMetadata.Port
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

  def flow(key: Key): String =
    button(
      icon = "bi-link-45deg",
      call = routes.FlowController.flowChart(key),
      toolTip = "Show in flow page.")

  val editIcon = "bi-pencil-square"

  def edit(key: Key): String = {
    val call = if key.keyMetadata == Port then
      routes.EditController.index(key.keyMetadata)
    else
      routes.EditController.edit(key)
    button(
      icon = editIcon,
      call = call,
      toolTip = "Edit this field.")
  }

  def rollback(): String =
    buildRollback(None)

  def rollback(Key: Key): String =
    buildRollback(Option(Key))

  def buildRollback(Key: Option[Key]): String =
    val icon = "bi-arrow-counterclockwise"
    Key match
      case Some(key) =>
        button(icon,
          routes.DataStoreController.rollbackOne(key),
          s"Rollback $Key Remove candidate restore original value.")
      case None =>
        button(icon,
          routes.DataStoreController.rollback(),
          "Rollback all fields.")

  def upload(doCandidateOnly: Boolean = true): String =
    buildUpload(None, doCandidateOnly)

  def upload(Key: Key): String =
    buildUpload(Option(Key))

  def buildUpload(Key: Option[Key], doCandidateOnly: Boolean = true): String =
    val icon = "bi-upload"
    Key match
      case Some(key) =>
        button(icon,
          routes.UploadController.start(UploadRequest(key)),
          s"Upload ${key.display} and accept the candidate.")
      case None =>
        button(icon,
          routes.UploadController.start(UploadRequest(doCandidateOnly = doCandidateOnly)),
          s"Upload all fields")
