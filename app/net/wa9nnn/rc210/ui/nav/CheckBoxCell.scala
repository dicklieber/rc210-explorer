package net.wa9nnn.rc210.ui.nav

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.FieldKey
import play.api.mvc.Call
import play.twirl.api.Html

object CheckBoxCell:
  def apply(boolean: Boolean): Cell =
    val ck: String = if (boolean)
      """checked="checked"""
    else {
      ""
    }
    val s = s"""<input class="form-check-input" type="checkbox" disabled="disabled" $ck>"""
    Cell.rawHtml(s)
      .withCssClass("checkbox")

