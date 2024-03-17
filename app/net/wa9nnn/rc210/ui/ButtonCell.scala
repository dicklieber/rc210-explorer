package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.FieldKey
import play.twirl.api.Html
import net.wa9nnn.rc210.ui.*

/**
 * Two buttons in a single [[Cell]].
 */
object ButtonCell:

  def editUploadFlow(fieldKey: FieldKey): Cell = buttonBuilder(
    Button.edit(fieldKey),
    Button.upload(fieldKey),
    Button.flow(fieldKey)
  )

  def rollback(): Cell = buttonBuilder(
    Button.rollback()
  )

  def edit(fieldKey: FieldKey): Cell = buttonBuilder(
    Button.edit(fieldKey)
  )

  def upload(fieldKey: FieldKey, doCandidateOnly:Boolean): Cell = buttonBuilder(
    Button.upload(fieldKey)
  )

  def upload(doCandidateOnly:Boolean): Cell = buttonBuilder(
    Button.upload(doCandidateOnly)
  )

  def editFlow(fieldKey: FieldKey): Cell = buttonBuilder(
    Button.edit(fieldKey),
    Button.flow(fieldKey),
  )

  def rollbackUpload(doCandidateOnly:Boolean): Cell = buttonBuilder(
    Button.upload(doCandidateOnly),
    Button.rollback()
  )
  def uploadRollback(fieldKey: FieldKey): Cell = buttonBuilder(
    Button.upload(fieldKey),
    Button.rollback(fieldKey)
  )

  def editHtml(fieldKey: FieldKey): String = Button.edit(fieldKey)

  def buttonBuilder(buttonHtmls: String*): Cell =
    val str: String = buttonHtmls.mkString("")
    Cell.rawHtml(str)





