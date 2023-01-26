package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.bubble.{D3Data, D3Node}
import net.wa9nnn.rc210.model.DatFile

import javax.inject.{Inject, Singleton}
import scala.collection.mutable

@Singleton
class D3DataBuilder @Inject()(functions: Functions) {
  def aoply(datFile: DatFile): D3Data = {
    val nodebuilder: mutable.Builder[D3Node, Set[D3Node]] = Set.newBuilder[D3Node]
    datFile.d3Nodes
    datFile.schedules.foreach(nodebuilder += _.d3Node)
    datFile.macros.foreach(nodebuilder += _.d3Node)


    val invokedFunctions = datFile.macros
      .flatMap(_.functions)

    val functionNodes = invokedFunctions
      .flatMap(functions.get(_))
      .map(_.d3Node)

    nodebuilder ++=functionNodes


    val nodes = nodebuilder.result().toList
    val links = nodes.flatMap(_.links)
    D3Data(nodes, links)
  }
}
