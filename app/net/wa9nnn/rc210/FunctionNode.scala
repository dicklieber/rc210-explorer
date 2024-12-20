package net.wa9nnn.rc210

import play.api.libs.json.*

trait FunctionNode extends Ordered[FunctionNode]:
  val key: Key
  val description: String

  override def compare(that: FunctionNode): Int = description compareTo that.description

  override def toString: String = s"$description (${key.rc210Number})"

object FunctionNode {
  implicit val fmtFunction: Format[FunctionNode] = new Format[FunctionNode] {
    override def reads(json: JsValue): JsResult[FunctionNode] = {

      try {
        val key: Key = (json \ "key").as[Key]

        val description: String = (json \ "description").as[String]
        val maybeDestination: Option[Key] = (json \ "destination").asOpt[Key]
        val f: FunctionNode = (maybeDestination match
          case Some(destinstion) =>
            TriggerFunctionNode(key, description, destinstion)
          case None =>
            SimpleFunctionNode(key, description)
          )
        JsSuccess(f)
      }
      catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }

    override def writes(key: FunctionNode): JsValue = {
      JsString(key.toString)
    }
  }

}