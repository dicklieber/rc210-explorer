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

package net.wa9nnn.rc210.data.vocabulary

import net.wa9nnn.rc210.key.WordKey
import org.specs2.mutable.Specification

class VocabularySpec extends Specification {
  "Vocabulary" should {

    "byText" in {
      Vocabulary("Zero") must beEqualTo (Phrase(WordKey(1), "Zero"))
      val phrase = Vocabulary("DVR10")
      phrase.wordKey.number must beEqualTo (246)
      phrase.string must beEqualTo ("DVR10")
    }

    "byNumber" in {
      Vocabulary(WordKey(1)).string must beEqualTo ("Zero")
    }
  }
}
