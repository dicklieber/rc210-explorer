package net.wa9nnn.rc210

case class TriggerFunctionNode(key: Key, description: String, destination: Key) extends FunctionNode:
  assert(destination.keyMetadata == KeyMetadata.Macro || destination.keyMetadata == KeyMetadata.Message, s"destination must be Key or MessageKey! But got: $key")
