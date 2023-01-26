package net.wa9nnn.rc210.model

import net.wa9nnn.rc210.bubble.{D3Node, NodeId}

trait  Node {
  val nodeId: NodeId
  /**
   * What this node can invoke.
   */
  def d3Node:D3Node
}
