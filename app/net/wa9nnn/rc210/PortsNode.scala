package net.wa9nnn.rc210

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.data.SimpleFieldNode
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import net.wa9nnn.rc210.ui.EditButtonCell
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.Html

object PortsNode extends SimpleFieldNode(KeyKind.Port):

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val rows: Seq[PortRow] = fieldEntries
      .groupBy(_.fieldKey.fieldName)
      .toSeq
      .sortBy(_._1)
      .map { (name, portEntries) =>
        PortRow(name, portEntries.sortBy(_.fieldKey.key.rc210Value))
      }
    views.html.ports(rows)

  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    throw new NotImplementedError() //not used

  override def editButtonCell(fieldKey: FieldKey): Cell =
    EditButtonCell(controllers.routes.EditController.index(KeyKind.Port))

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
case class PortRow(name: String, portEntries: Seq[FieldEntry]):
  def cells: Seq[String] =
    portEntries.map { fe =>
      val v: FieldValue = fe.value
      v.toHtmlField(fe.fieldKey)
    }

