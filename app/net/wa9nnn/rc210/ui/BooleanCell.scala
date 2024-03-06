package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import views.html.boolean

object BooleanCell:
  def  apply(value:Boolean):Cell =
    Cell.rawHtml(boolean(value).body)    
