package net.wa9nnn.rc210

import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.data.SimpleFieldNode
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.ui.{ButtonCell, FormField}
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.Html

object PortsNode extends SimpleFieldNode(KeyKind.Port):

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    def rows: Seq[Row] = fieldEntries
      .groupBy(_.fieldKey.fieldName)
      .toSeq
      .sortBy(_._1)
      .map { (name, portEntries) =>
        PortRow(name, portEntries.sortBy(_.fieldKey.key.rc210Value)).toRow
      }

    val colHeaders: Seq[Any] =
      val value: Seq[Cell] = Key.portKeys.map(key =>
        Cell(key.keyWithName))

      value.prepended(Cell("Field"))

    val header = Header.singleRow(colHeaders: _*)
    val namesRow: Row = Row(Key.portKeys.map { key =>
      FormField(FieldKey(key, NamedKey.fieldName), key.name)
    }.prepended(Cell("Name")))
    val table = Table(
      header,
      rows.prepended(namesRow)
    )

    views.html.ports(table)

  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    throw new NotImplementedError() //not used
  
  override def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate] = {
    val value: Seq[UpdateCandidate] = collect(data)
    value
  }

/**
 * One <tr> of data used by [[views.html.ports]]
 *
 * @param name        row header
 * @param portEntries remaining td elements.
 */
case class PortRow(name: String, portEntries: Seq[FieldEntry]) extends RowSource:
  def toRow: Row =
    Row(portEntries.map { fe =>
      fe.toEditCell
    }.prepended(Cell(name))
    )

