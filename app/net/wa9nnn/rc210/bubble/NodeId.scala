package net.wa9nnn.rc210.bubble

case class NodeId(prefix:Char, number:Int){
  override def toString: String = {
    s"$prefix$number"
  }
}
