package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Row, Table, TableInACell, TableSection}
import net.wa9nnn.rc210.FieldKey
import play.api.mvc.Call
import play.twirl.api.Html
import views.html.flowChartButton

/**
 *
 * @param sectionName for section header row.
 * @param buttons     to be shown in header row
 * @param newRows     the body rows.
 */
class TableSectionButtons(sectionName: String, buttonCells: Cell*)(newRows: (Row | (String, Any))*) extends TableSection:

  override def rows: Seq[Row] =
    val sectionRow: Row =
      new Row(Seq(TableInACell(
        Table(Seq.empty,
          Seq(Row(Cell(sectionName), buttonCells))
        )
      )))

    sectionRow +:
      newRows.map {
        {
          case r: Row =>
            r
          case (name: String, value: Any) =>
            Row(name, Cell(value))

        }
      }

object TableSectionButtons:

  def apply(fieldKey: FieldKey, newRows: Row*) =
    new TableSectionButtons(fieldKey.toString,
      fieldKey.editButtonCell)(newRows: _*)

