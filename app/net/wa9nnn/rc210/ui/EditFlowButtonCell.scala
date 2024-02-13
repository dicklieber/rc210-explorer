package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.FieldKey
import play.api.mvc.Call
import play.twirl.api.Html
import views.html.{editButton, editFlowButtions}

object EditFlowButtonCell:
  def apply(fieldKey: FieldKey): Cell = {
    val html: String = editFlowButtions(fieldKey).toString
    Cell.rawHtml(html)
  }


