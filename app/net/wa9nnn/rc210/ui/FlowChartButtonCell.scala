package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.{FieldKey, Key}
import play.api.mvc.Call
import play.twirl.api.Html
import views.html.flowChartButton

object FlowChartButtonCell:
  def apply(key: Key): Cell = {
    val html: String = flowChartButton(key).toString
    Cell.rawHtml(html)
  }


