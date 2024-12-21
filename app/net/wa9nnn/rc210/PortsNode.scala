package net.wa9nnn.rc210

import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.data.SimpleFieldNode
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.ui.{ButtonCell, FormData, FormField}
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.Html

object PortsNode extends SimpleFieldNode(KeyMetadata.Port):

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val rows: Seq[Row] = fieldEntries
      .groupBy(_.key.qualifier.get)
      .toSeq
      .sortBy((t: (String, Seq[FieldEntry])) => t._1)
      .map { (name, portEntries) =>
        PortRow(name, portEntries.sortBy(_.key.rc210Number)).toRow
      }

    val colHeaders: Seq[Any] =
      val value: Seq[Cell] = Key.portKeys.map(key =>
        Cell(key.keyWithName))

      value.prepended(Cell("Field"))

    val header = Header.singleRow(colHeaders: _*)
    val namesRow: Row = Row(Key.portKeys.map { key =>
//      val fieldKey = PortFieldKey(key, NamedKey.fieldName)
      FormField(key, key.name)
    }.prepended(Cell("Name")))
    val table = Table(
      header,
      rows.prepended(namesRow)
    )

    views.html.ports(table)

  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    throw new NotImplementedError() //not used
  
  override def bind(formData: FormData): Seq[UpdateCandidate] = 
    val value: Seq[UpdateCandidate] = collect(formData)
    value

/**
 * One <tr> of data used by [[views.html.ports]]
 *
 * @param name        row header
 * @param portEntries remaining td elements.
 */
case class PortRow(name: String, portEntries: Seq[FieldEntry]) :
  def toRow: Row =
    Row(portEntries.map { fe =>
      val v: FieldValue = fe.value
//      v.toEditCell(fe.fieldKey)
      v.displayCell
    }.prepended(Cell(name))
    )

