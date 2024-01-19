package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.html.renderTable
import com.wa9nnn.wa9nnnutil.tableui.{Header, Row, Table, TableSection}
import controllers.routes.*
import net.wa9nnn.rc210.{Key, RcSpec}
import play.twirl.api.Html
import views.html.*

class TableSectionButtonsTest extends RcSpec {

  "TableSectionButtons" should {
    val searchKey = Key.timerKeys.head
    val html: Html = editButton(ClockController.index)
    val html1 = flowChartButton(searchKey)
    val tsb: TableSectionButtons = new TableSectionButtons("Header",
      html,
      html1
    )
    "rows" in {
      val ts: TableSection = tsb(Seq(
        "r0" -> 0,
        Row("r1", 1)
      ))
      val rows = ts.rows
      val headerRow = rows.head
      val headCell = headerRow.cells.head
      headCell

      val table = Table(Header("KV Top"), Seq(ts))
      val appendable = renderTable(table)
      val string = appendable.toString
      string

    }
  }
}
