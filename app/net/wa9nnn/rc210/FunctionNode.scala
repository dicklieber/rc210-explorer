package net.wa9nnn.rc210

import play.api.libs.json.*

case class FunctionNode(key: Key, description: String, destination: Option[Key]) :

  override def toString: String =
    val destStr = destination.map(d => s" $description (${key.rc210Number}) -> $d").getOrElse("")
    s"$description (${key.rc210Number})$destStr"

/*object FunctionNode {
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

}*/