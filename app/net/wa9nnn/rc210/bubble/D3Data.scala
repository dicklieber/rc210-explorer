package net.wa9nnn.rc210.bubble

import play.api.libs.json.{Json, OFormat}

case class D3Data(nodes: Seq[D3Node] = Seq.empty, links: Seq[D3Link] = Seq.empty)

case class D3Node(nodeId:NodeId,
                   top: NodeBubbleLine,
                   middle: NodeBubbleLine,
                   bottom: NodeBubbleLine,
                   cssClass: String)

object D3Node {
//  def apply(nodeDbData: NodeDbData): D3Node = {
//    new D3Node(nodeDbData.nodeId,
//      NodeBubbleLine(nodeDbData.nodeId.nodeId, "bubbleNodeId"),
//      NodeBubbleLine(nodeDbData.info, "bubbleLineInfo"),
//      NodeBubbleLine(nodeDbData.callsign, "bubbleLineCallsign"),
//      nodeDbData.nodeId.cssClassName
//    )
//  }
}

case class NodeBubbleLine(text: String, cssClass: String)

case class D3Link(source_id: NodeId, target_id: NodeId)

object D3Link {
//  def apply(source: NodeDbData, target: NodeDbData): D3Link = {
//    new D3Link(source.nodeId, target.nodeId)
//  }
}

object D3Data {
//  implicit val fmtNodeBubbleLine: OFormat[NodeBubbleLine] = Json.format
//  implicit val fmtData: OFormat[D3Node] = Json.format[D3Node]
//  implicit val fmtLink: OFormat[D3Link] = Json.format[D3Link]
//  implicit val fmt: OFormat[D3Data] = Json.format[D3Data]
}

