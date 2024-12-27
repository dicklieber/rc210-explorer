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

package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.KeyMetadata.Common
import net.wa9nnn.rc210.{Key, KeyMetadata, NamedKey, RcSpec, WithTestConfiguration}

class NamedIKeyManagerTest extends WithTestConfiguration {

  private val aMacroKey: Key = Key.macroKeys.head
  private val startupMacro = NamedKey(aMacroKey, "startup Macro")

  "NamedKeyManagerTest" should
    {
      def newNamedKeyManager(): NamedKeyManager =
        new NamedKeyManager(tempDir("namedKey.json").toAbsolutePath.toString)

      "save/load" in
        {
          val namedKeyManager = newNamedKeyManager()
          namedKeyManager.update(startupMacro)
          val saved = namedKeyManager.namedKeys
          saved must have size 1
          val path = namedKeyManager.path
          val loaded = new NamedKeyManager(path)

          val namedKeys = loaded.namedKeys
          namedKeys must have size 1
          
        }

      "namedKey" when
        {
          val namedKeyManager = newNamedKeyManager()
          val aKey = Key(KeyMetadata.Port,1,"Color")
          namedKeyManager.update(NamedKey(aKey, "Red"))
          "name exists" in
            {
              namedKeyManager.nameForKey(aKey) mustBe "Red"
            }
          "name no name" in
            {
              namedKeyManager.nameForKey(Key(Common,"flavor")) mustBe ""
            }
        }
    }
}
