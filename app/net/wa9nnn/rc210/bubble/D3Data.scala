package net.wa9nnn.rc210.bubble

import play.api.libs.json.{Json, OFormat}

case class D3Data(nodes: Seq[D3Node] = Seq.empty, links: List[D3Link] = List.empty)

case class D3Node(id: NodeId,
                  info: String,
                  links:List[D3Link] = List.empty)

object D3Node {
//  def apply(nodeId: NodeId, info: String, links:List[NodeId] = List.empty): D3Node = {
//    new D3Node(nodeId,info,
//      links.map{dest => D3Link(nodeId, dest)}
//    )
//  }
}

//case class NodeBubbleLine(text: String, cssClass: String)

case class D3Link(source: NodeId, target: NodeId)

//object D3Link {
//  //  def apply(source: NodeDbData, target: NodeDbData): D3Link = {
//  //    new D3Link(source.nodeId, target.nodeId)
//  //  }
//}

object D3Data {
//  implicit val fmtNodeBubbleLine: OFormat[NodeBubbleLine] = Json.format
  implicit val fmtLink: OFormat[D3Link] = Json.format[D3Link]
  implicit val fmtData: OFormat[D3Node] = Json.format[D3Node]
  implicit val fmt: OFormat[D3Data] = Json.format[D3Data]
}

