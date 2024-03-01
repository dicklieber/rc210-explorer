package net.wa9nnn.rc210.data

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.field.{FieldEntry, SimpleFieldValue}
import net.wa9nnn.rc210.ui.EditButtonCell
import net.wa9nnn.rc210.{FieldKey, KeyKind}
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import views.html.common

case object CommonNode extends SimpleFieldNode(KeyKind.Port):
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

  def header(count: Int): Header =
    Header(s"Common ($count)", "field", "Value")

  override def editButtonCell(fieldKey: FieldKey): Cell =
    EditButtonCell(controllers.routes.EditController.index(KeyKind.Common))

  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    throw new NotImplementedError() //Not used; index is edit.

    
