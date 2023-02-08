package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.command.ItemValue
import play.api.libs.json.{Json, OFormat}

case class Rc210Data(itemValues:Seq[ItemValue])

object Rc210Data {
  import ItemValue.fmtItemValue
  import ItemValue.fmtL10NError
  implicit val fmtRc210Data: OFormat[Rc210Data] = Json.format[Rc210Data]
}
