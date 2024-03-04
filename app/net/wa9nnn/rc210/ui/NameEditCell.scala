package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.*
import views.html.nameEdit

object NameEditCell:
  def apply(key: Key): Cell = {
    val html: String = nameEdit(key).toString
    Cell.rawHtml(html)
  }


