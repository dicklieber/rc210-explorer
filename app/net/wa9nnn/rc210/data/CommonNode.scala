package net.wa9nnn.rc210.data

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.{FieldEntry, FieldValueSimple}
import net.wa9nnn.rc210.ui.FormData
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import views.html.common

case object CommonNode extends SimpleFieldNode(KeyMetadata.Common):
  def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val table = Table(header(fieldEntries.length),
      fieldEntries.map { fieldEntry =>
        Row(
          fieldEntry.key.qualifier.getOrElse("XYZZY"),
//          fieldEntry.value[FieldValueSimple].toEditCell(fieldEntry.fieldKey)
          fieldEntry.value[FieldValueSimple].displayCell //todo edit cell??
        )
      }
    )
    common(table)

  override def bind(formData: FormData): Seq[UpdateCandidate] = {
    val value: Seq[UpdateCandidate] = collect(formData)
    value
  }

  def header(count: Int): Header =
    Header(s"Common ($count)", "field", "Value")
  
  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =

    val table = Table(Header.none,
      Seq(
        Row(
          fieldEntry.key.qualifier.getOrElse("XYZZY"),
          fieldEntry.value[FieldValueSimple].displayCell // todo (fieldEntry.fieldKey)
//          fieldEntry.value[FieldValueSimple].toEditCell(fieldEntry.fieldKey)
        )
      )
    
    )
    common(table)
       

    
