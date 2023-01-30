package net.wa9nnn.rc210.data

import com.wa9nnn.util.tableui.{Header, Row, RowSource}
import net.wa9nnn.rc210.NumberedValues
import net.wa9nnn.rc210.bubble.NodeId
import net.wa9nnn.rc210.model.DatFile

import scala.util.Try

case class MessageMacroNode(nodeId: MessageMacroId, value: String) extends RowSource {
  override def toRow: Row = Row(nodeId.toCell, value)
}

object MessageMacroNode {
  def header: Header = Header("Message Macros", "Id", "Value")
}

object MessageMacroNodes {
  def apply(datFile: DatFile): MessageMacros = {
    MessageMacros(datFile.section("MessageMacros")
      .process[MessageMacroNode] { numberedValues =>
        build(numberedValues)
      }.filter(_.value != "NONE STORED")
    )
  }

  private def build(implicit numberedValues: NumberedValues): Try[MessageMacroNode] = {
    {
      import NumberedValues._
      Try {
        val messageMacro = MessageMacroNode(
          nodeId = MessageMacroId(numberedValues.number.get),
          value = vs("MessageMacro")
        )
        messageMacro
      }
    }
  }
}

case class MessageMacroId(override val number: Int) extends NodeId {
  override val prefix: Char = 'M'
  override val cssClass: String = "messageMacro"


}

object MessageMacroId {
  def apply(number: Int): MessageMacroId = new MessageMacroId(number)
}

case class MessageMacros(messaageMacros: List[MessageMacroNode]) {
  val map: Map[MessageMacroId, MessageMacroNode] = messaageMacros.map(m => m.nodeId -> m).toMap

  def apply(messageMacroId: MessageMacroId): MessageMacroNode = map(messageMacroId)
}


