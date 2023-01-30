package net.wa9nnn.rc210.model

import net.wa9nnn.rc210.DatSection
import net.wa9nnn.rc210.data.{MessageMacroNodes, MessageMacros, ScheduleNode, Schedules}
import net.wa9nnn.rc210.model.macros.{MacroNode, Macros}


/**
 * Internalized representation of a RCP .dat file
 * as a map of [[DatSection]]s.
 */
class DatFile(sections: Seq[DatSection]) {

  lazy val schedules: Seq[ScheduleNode] = Schedules(this)
  lazy val macros: Seq[MacroNode] = Macros(this)
  lazy val messageMacros: MessageMacros = MessageMacroNodes(this)


  def size: Int = sections.size

  val map: Map[String, DatSection] = sections.map(datSection => datSection.sectionName -> datSection).toMap


  //    val macroNodeBuilder: mutable.Builder[D3Node, List[D3Node]] = List.newBuilder[D3Node]
  //    val functionNodeIdBuilder: mutable.Builder[FunctionNodeId, Set[FunctionNodeId]] = Set.newBuilder[FunctionNodeId]
  //    val schedules: Seq[Schedule] = Schedules(this)
  //    val macros: Seq[Macro] = Macros(this)
  //
  //    schedules.foreach(macroNodeBuilder += _.d3Node)
  //    macros.foreach { m =>
  //      macroNodeBuilder += m.d3Node
  //      val value: List[NodeId] = m.functions.flatMap(_.callMacro)
  //      functionNodeIdBuilder ++= value
  //    }
  //
  //    val nodes = macroNodeBuilder.result()
  //
  //    val links: List[] = nodes.flatMap { d3Node =>
  //      d3Node.links.map{}
  //    }
  //
  //    D3Data(nodes, links)
  //    }


  def section(sectionName: String): DatSection = {
    map(sectionName)
  }

  def head: DatSection = sections.head

  def dump(): Unit = {
    sections.foreach(_.dump())
  }
}
