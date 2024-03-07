package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.FieldKey

object RollbackButton:
  def apply(fieldKey: FieldKey): Cell =
    val call = controllers.routes.DataStoreController.rollbackOne(fieldKey)

    val html =
      s"""<button type="button" class="bi bi-arrow-counterclockwise" onclick="window.location.href='${call.url}'">
    </button>"""
    Cell.rawHtml(html)  


