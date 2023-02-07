package net.wa9nnn.rc210.command.algorithm

import net.wa9nnn.rc210.command.ItemValue
import play.api.libs.json.{Format, Json}

/**
 * Instructions how to render/edit an [[ItemValue]].
 */

object RenderAlgorithm extends Enumeration {

  type Render = Value

  val dtmf: RenderAlgorithm.Value = Value("dtmf")
  val bool: RenderAlgorithm.Value = Value("bool")
  val int8: RenderAlgorithm.Value = Value("int8")
  val int16: RenderAlgorithm.Value = Value("int16")
  val range: RenderAlgorithm.Value = Value("range")

  implicit val format1: Format[Render] = Json.formatEnum(RenderAlgorithm)


  object DisplayAlgorithm {
    def apply(itemValue: ItemValue): String =
      itemValue.renderAlgo match {
        case RenderAlgorithm.dtmf => "todo"
        case RenderAlgorithm.bool => "todo"
        case RenderAlgorithm.int8 => "todo"
        case RenderAlgorithm.int16 => "todo"
        case RenderAlgorithm.range => "todo"
      }
  }
}

