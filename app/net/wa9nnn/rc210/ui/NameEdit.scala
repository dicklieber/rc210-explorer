package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui.Cell
import net.wa9nnn.rc210.*
import play.twirl.api.Html

import scala.xml.*


object NameEdit:
  /**
   * Use in a [[Table]].
   * @param key
   * @return
   */
  def cell(key: Key): Cell =
    Cell.rawHtml(html(key).body)

  /**
   * Use in twirl template e.g. @NameEdit.html(key)
   * @param key
   * @return
   */
  def html(key: Key): Html =
    val namedKey: NamedKey = key.namedKey
    val id: String = namedKey.fieldKey.id

    val r: Elem =
      <div>
        {namedKey.key.rc210Number}: Name:<input name={id} value={namedKey.name}></input>
      </div>
    val s = r.toString
    Html(s)