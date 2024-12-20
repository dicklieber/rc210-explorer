/*
 * Copyright (c) 2024. Dick Lieber, WA9NNN
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
 *
 */

package net.wa9nnn.rc210.data.macros

import net.wa9nnn.rc210.data.Dtmf.Dtmf
import net.wa9nnn.rc210.{Key, KeyMetadata, RcSpec, WithMemory}

class DtmfMacroExtractorTest extends WithMemory {

  "DtmfMacroExtractorTest" should
    {
      "apply" in
        {
          val dtmfMacros = DtmfMacroExtractor.apply(memory)
          val macro1 = Key(KeyMetadata.Macro, 1)
          val maybeDtmf: Option[Dtmf] = dtmfMacros.apply(macro1)
          maybeDtmf.value.toString mustBe "10901"
        }
    }
}
