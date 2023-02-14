package net.wa9nnn.rc210.data

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.MessageMacroKey
import net.wa9nnn.rc210.model.Node

import scala.util.Try

case class MessageMacroNode(key: MessageMacroKey, value: String) extends Node with RowSource {
  override def toRow: Row = Row(key.toCell, value)
}

object MessageMacroNode {
  def header: Header = Header("Message Macros", "Id", "Value")
}

object MessageMacroNodes {
/*
  def apply(datFile: DatFile): MessageMacros = {
    MessageMacros(datFile.section("MessageMacros")
      .process[MessageMacroNode] { numberedValues =>
        build(numberedValues)
      }.filter(_.value != "NONE STORED")
    )
  }
*/

/*
  private def build(implicit numberedValues: NumberedValues): Try[MessageMacroNode] = {
    {
      Try {
        val messageMacro = MessageMacroNode(
          key = MessageMacroKey(numberedValues.number.get),
          value = vs("MessageMacro")
        )
        messageMacro
      }
    }
  }
*/
}



case class MessageMacros(messaageMacros: List[MessageMacroNode]) {
  val map: Map[MessageMacroKey, MessageMacroNode] = messaageMacros.map(m => m.key -> m).toMap

  def apply(key: MessageMacroKey): MessageMacroNode = map(key)
}


