package net.wa9nnn.rc210.command

import com.wa9nnn.util.JsonFormatUtils.javaEnumFormat
import play.api.libs.json.Format

object CommandFormats {
  implicit val fmtLocus: Format[Locus] = javaEnumFormat[Locus]
  implicit val fmtValueType: Format[ValueType] = javaEnumFormat[ValueType]
  implicit val fmtCommandId: Format[CommandId] = javaEnumFormat[CommandId]

}


