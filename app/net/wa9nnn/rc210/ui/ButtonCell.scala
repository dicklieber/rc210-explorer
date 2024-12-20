package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.Key
import play.twirl.api.Html
import net.wa9nnn.rc210.ui.*

/**
 * Two buttons in a single [[Cell]].
 */
object ButtonCell:

  def editUploadFlow(key: Key): Cell = buttonBuilder(
    Button.edit(key),
    Button.upload(key),
    Button.flow(key)
  )

  def rollback(): Cell = buttonBuilder(
    Button.rollback()
  )

  def edit(key:Key): Cell = buttonBuilder(
    Button.edit(key)
  )

  def upload(key:Key, doCandidateOnly:Boolean): Cell = buttonBuilder(
    Button.upload(key)
  )

  def upload(doCandidateOnly:Boolean): Cell = buttonBuilder(
    Button.upload(doCandidateOnly)
  )

  def editFlow(key:Key): Cell = buttonBuilder(
    Button.edit(key),
    Button.flow(key),
  )

  def rollbackUpload(doCandidateOnly:Boolean): Cell = buttonBuilder(
    Button.upload(doCandidateOnly),
    Button.rollback()
  )
  def uploadRollback(key:Key): Cell = buttonBuilder(
    Button.upload(key),
    Button.rollback(key)
  )

  def editHtml(key:Key): String = Button.edit(key)

  def buttonBuilder(buttonHtmls: String*): Cell =
    val str: String = buttonHtmls.mkString("")
    Cell.rawHtml(str)





