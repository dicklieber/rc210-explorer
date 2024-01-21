package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row, TableSection}
import net.wa9nnn.rc210.FieldKey
import play.api.mvc.Call
import play.twirl.api.Html
import views.html.{editButton, flowChartButton}

/**
 *
 * @param sectionName for section header row.
 * @param buttons     to be shown in header row
 * @param newRows     the body rows.
 */
class TableSectionButtons(sectionName: String, buttons: Html*)(newRows: (Row | (String, Any))*) extends TableSection:

  override def rows: Seq[Row] =
    val buttonHtml: Seq[String] = buttons.map(_.toString)
    val str = buttonHtml.mkString("")
    val cell: Cell = Cell.rawHtml(
        <p>
           <span>
            {str}
          </span>
          <span>
            {sectionName}
          </span>

        </p>.text
      )
      .withCssClass("sectionHeader")
      .withColSpan(2)
    val readerRow: Row = Row(Seq(cell))
    readerRow +:
      newRows.map {
        {
          case r: Row =>
            r
          case (name: String, value: Any) =>
            Row(name, Cell(value))

        }
      }

object TableSectionButtons:

  def apply(fieldKey: FieldKey, edit: Call, newRows: Row | (String, Any)*): TableSection =
//    val flowChart = flowChartButton(fieldKey.key)

    new TableSectionButtons(fieldKey.toString,
      editButton(edit))(newRows: _*)

