package net.wa9nnn.rc210.ui.flow

import net.wa9nnn.rc210.{FieldKey, Key, NamedKey}
import play.api.libs.json.{Format, Json}

case class D3Data(nodes: Seq[D3Node], links: Seq[D3Link])

object D3Data:
  implicit val fmtD3Data: Format[D3Data] = Json.format[D3Data]

case class D3Link(a: String, b: String)

object D3Link:
  implicit val fmtD3Link: Format[D3Link] = Json.format[D3Link]

case class D3Node(nodeKey:String, name:String, topLine: String, detail: String, cssClass:String = "")

object D3Node:
  implicit val fmtD3Node: Format[D3Node] = Json.format[D3Node]
  