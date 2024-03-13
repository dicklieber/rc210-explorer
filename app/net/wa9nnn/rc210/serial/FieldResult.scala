package net.wa9nnn.rc210.serial

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table, TableInACell}
import net.wa9nnn.rc210.FieldKey
import net.wa9nnn.rc210.serial.comm.RcResponse

case class FieldResult(fieldKey: FieldKey, responses: Seq[RcResponse]) extends ProgressItem:
  val ok: Boolean =
    val errorCount: Int = responses.foldLeft(0)((accum, rcResponse) =>
      if (!rcResponse.ok)
        accum + 1
      else
        accum
    )
    errorCount == 0

  def toRow: Row =
    throw new NotImplementedError() //todo

  override def toRows: Seq[Row] = {
    if (responses.isEmpty)
      Seq.empty
    else
      val top: RcResponse = responses.head
      val row0 = new Row(top.detailCells.prepended(Cell(fieldKey.display)
        .withRowSpan(responses.length)))

      val tailRows = responses
        .tail
        .map { r =>
          Row(top.detailCells)
        }
      tailRows.prepended(row0)
  }

  def buildResultTable(items: Seq[ProgressItem]): Table =
    val fieldRows: Seq[Row] = items.flatMap { f => f.toRows }
    val header = Header.singleRow("Field", "Sent", "Response")
    Table(header, fieldRows)
   
  