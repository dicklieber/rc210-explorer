package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import play.api.mvc.Call
import play.twirl.api.Html
import views.html.editButton

object EditButtonCell:
  def apply(fieldKey: FieldKey): Cell = {
    val keyKind = fieldKey.key.keyKind
    val html = if(keyKind == KeyKind.Port)
       editButton(controllers.routes.EditController.index(keyKind)).toString
    else
      editButton(controllers.routes.EditController.edit(fieldKey)).toString
    Cell.rawHtml(html)
  }


