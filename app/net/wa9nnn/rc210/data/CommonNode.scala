package net.wa9nnn.rc210.data

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{FieldEntry, SimpleFieldValue}
import net.wa9nnn.rc210.ui.{EditButton, TabE}
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import views.html.common

case object CommonNode extends SimpleFieldNode(KeyKind.Common):
  def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val table = Table(header(fieldEntries.length),
      fieldEntries.map { fieldEntry =>
        Row(
          fieldEntry.fieldKey.fieldName,
          fieldEntry.value[SimpleFieldValue].toEditCell(fieldEntry.fieldKey)
        )
      }
    )
    common(table)

  override def bind(data: Map[String, Seq[String]]): Seq[UpdateCandidate] = {
    val value: Seq[UpdateCandidate] = collect(data)
    value
  }

  def header(count: Int): Header =
    Header(s"Common ($count)", "field", "Value")

  override def editButtonCell(fieldKey: FieldKey): Cell =
    EditButton.cell(fieldKey)

  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =

    val table = Table(Header.none,
      Seq(
        Row(
          fieldEntry.fieldKey.fieldName,
          fieldEntry.value[SimpleFieldValue].toEditCell(fieldEntry.fieldKey)
        )
      )
    
    )
    common(table)
       

    
