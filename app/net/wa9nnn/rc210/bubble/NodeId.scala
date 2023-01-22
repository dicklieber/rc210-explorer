package net.wa9nnn.rc210.bubble

/**
 * Unique ID for any node.
 * @param prefix kind of node e.g. 'm' macro 'f' function 't' timer
 * @param number within the kind of [[Node]]
 */
abstract class NodeId(prefix:Char, val number:Int){
  override def toString: String = {
    s"$prefix$number"
  }
}
