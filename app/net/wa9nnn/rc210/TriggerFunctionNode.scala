package net.wa9nnn.rc210

case class TriggerFunctionNode(key: Key, description: String, destination: Key) extends FunctionNode:
  assert(destination.keyKind == KeyKind.Macro || destination.keyKind == KeyKind.Message, s"destination must be Key or MessageKey! But got: $key")
