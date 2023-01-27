package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.NumberedValues
import net.wa9nnn.rc210.bubble.NodeId
import net.wa9nnn.rc210.model.{DatFile, Node}

import scala.util.Try

case class MessageMacro(nodeId: MessageMacroId, value: String) extends Node

object MessageMacro {
  def apply(datFile: DatFile): MessageMacros = {
    MessageMacros(datFile.section("MessageMacros")
      .process[MessageMacro] { numberedValues =>
        build(numberedValues)
      }.filter(_.value != "NONE STORED")
    )
  }

  private def build(implicit numberedValues: NumberedValues): Try[MessageMacro] = {
    {
      import NumberedValues._
      Try {
        val messageMacro = MessageMacro(
          nodeId = MessageMacroId(numberedValues.number.get),
          value = vs("MessageMacro")
        )
        messageMacro
      }
    }
  }

}

case class MessageMacroId(override val number: Int) extends NodeId('i', number, "messageMacro")

object MessageMacroId {
  def apply(number: Int): MessageMacroId = new MessageMacroId(number)
}

case class MessageMacros(messaageMacros: List[MessageMacro]) {
  val map: Map[MessageMacroId, MessageMacro] = messaageMacros.map(m => m.nodeId -> m).toMap

  def apply(messageMacroId: MessageMacroId): MessageMacro = map(messageMacroId)
}


