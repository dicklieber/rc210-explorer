package net.wa9nnn.rc210

import com.wa9nnn.wa9nnnutil.tableui.Row
import net.wa9nnn.rc210.data.{EditHandler, SimpleFieldNode}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValue}
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.{Html, HtmlFormat}

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

  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider) = ???

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

