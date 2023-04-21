/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.data.datastore.UpdateData
import net.wa9nnn.rc210.data.message.Message
import net.wa9nnn.rc210.key.KeyFactory.MessageKey
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import play.api.mvc.AnyContentAsFormUrlEncoded

class FormParserSpec extends Specification {
  "RequestParserSpec" should {

    "RequestParser without name" should {
      val content = AnyContentAsFormUrlEncoded(Seq(
        "words:messageKey1" -> Seq("1,2,3")
      ).toMap)
      val r: UpdateData = FormParser[MessageKey](content, (key: MessageKey, kv: Map[String, String]) => {

        val value1: String = kv("words")
        val words: Array[Int] = value1.split(",").filter(_.nonEmpty).map(_.toInt)

        Message(key, words)
      }
      )
      r.candidates must haveLength(1)
      r.names must Matchers.haveLength(0)
    }
    "RequestParser with name" should {
      val content = AnyContentAsFormUrlEncoded(Seq(
        "name:messageKey1" -> Seq("Groucho"),
        "words:messageKey1" -> Seq("1,2,3")
      ).toMap)
      val r: UpdateData = FormParser[MessageKey](content, (key: MessageKey, kv: Map[String, String]) => {

        val value1: String = kv("words")
        val words: Array[Int] = value1.split(",").filter(_.nonEmpty).map(_.toInt)

        Message(key, words)
      }
      )
      r.candidates must haveLength(1)
      r.names must Matchers.haveLength(1)
      val namedKey = r.names.head
      namedKey.toString must beEqualTo("NamedKey(messageKey1,Groucho)")
    }
  }
}