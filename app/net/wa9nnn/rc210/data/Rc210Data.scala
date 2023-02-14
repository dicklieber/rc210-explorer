package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.{Key, MacroKey, Named}
import net.wa9nnn.rc210.command.ItemValue
import net.wa9nnn.rc210.data.macros.MacroNode
import net.wa9nnn.rc210.data.schedules.Schedule
import net.wa9nnn.rc210.data.vocabulary.MessageMacro
import net.wa9nnn.rc210.model.TriggerNode
import play.api.libs.json.{Json, OFormat}

/**
 *
 * @param itemValues simple items
 * @param macros
 * @param names
 */
case class Rc210Data(itemValues: Seq[ItemValue],
                     macros: Seq[MacroNode],
                     schedules: Seq[Schedule],
                     messageMacros: Seq[MessageMacro],
                     metadata: Metadata = Metadata()) {
  def triggers(macroKey: MacroKey): Seq[TriggerNode] = {
    //todo There are other TriggerNodes
    schedules
      .filter(_.enabled)
  }
}

case class Metadata(names: Seq[Named] = Seq.empty)

object Rc210Data {
  implicit val fmtMetadata: OFormat[Metadata] = Json.format[Metadata]
  implicit val fmtRc210Data: OFormat[Rc210Data] = Json.format[Rc210Data]
}
