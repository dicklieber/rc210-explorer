package net.wa9nnn.rc210.ui

import com.wa9nnn.wa9nnnutil.tableui._
import play.api.i18n.{Messages, MessagesProvider}

object KeyValueTable {
  /**
   * A two column table of key/value pairs, key is an  L10N label.
   * @param kv L10N key and its value. value is processed as a table [[Cell]]
   * @return
   */
  def table( kv: (String, Any)*)(implicit messagesProvider: MessagesProvider): Table = {
    val rows = kv.map { case(k, v) =>
      Row(Messages(k), v)
    }
    Table(Seq.empty, rows)
  }
}

