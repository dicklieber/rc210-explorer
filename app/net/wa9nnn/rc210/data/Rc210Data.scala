package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.command.{ItemValue, Named}
import net.wa9nnn.rc210.data.macros.Macro
import play.api.libs.json.{Json, OFormat}

/**
 *
 * @param itemValues simple items
 * @param macros
 * @param names
 */
case class Rc210Data(itemValues:Seq[ItemValue], macros:Seq[Macro], names:Seq[Named])

object Rc210Data {
  import net.wa9nnn.rc210.command.Key.fmtKey
  import net.wa9nnn.rc210.command.Named.fmtNamed
  import ItemValue.fmtItemValue
  import ItemValue.fmtL10NError
  implicit val fmtRc210Data: OFormat[Rc210Data] = Json.format[Rc210Data]
}
