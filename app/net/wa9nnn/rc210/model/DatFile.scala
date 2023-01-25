package net.wa9nnn.rc210.model

import net.wa9nnn.rc210.DatSection
import net.wa9nnn.rc210.bubble.{D3Data, D3Link, D3Node}
import net.wa9nnn.rc210.data.{Schedule, Schedules}
import net.wa9nnn.rc210.model.macros.{Macro, Macros}

import scala.collection.mutable


/**
 * Internalized representation of a RCP .dat file
 * as a map of [[DatSection]]s.
 */
class DatFile(sections: Seq[DatSection]) {
  def size: Int = sections.size


  private val map: Map[String, DatSection] = sections.map(datSection => datSection.sectionName -> datSection).toMap

  private val builder: mutable.Builder[D3Node, List[D3Node]] = List.newBuilder[D3Node]
  val schedules: Seq[Schedule] = Schedules(this)
  val macros: Seq[Macro] = Macros(this)

  schedules.foreach(builder += _.d3Node)
  macros.foreach(builder += _.d3Node)

  private val nodes: List[D3Node] = builder.result()


  val links: List[D3Link] = nodes.flatMap{ d3Node =>
    d3Node.links
  }


  val d3Data:D3Data = D3Data(nodes, links)


  def section(sectionName: String): DatSection = {
    map(sectionName)
  }

  def head: DatSection = sections.head

  def dump(): Unit = {
    sections.foreach(_.dump())
  }
}
