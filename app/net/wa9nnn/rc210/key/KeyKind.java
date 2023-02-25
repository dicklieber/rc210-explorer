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

package net.wa9nnn.rc210.key;

public enum KeyKind {

    alarmKey(5, "alarm"),
    dtmfMacroKey(5, "dtmfMacro"),
    functionKey(1005, "function"),
    macroKey(105, "macro"),
    messageMacroKey(90, "messageMacro"),
    miscKey(1, "misc"),
    portKey(3, "port"),
    scheduleKey(40, "schedule"),
    wordKey(256, "word");

    private final int maxN;
    private final String name;

    KeyKind(int maxN, String name) {
        this.maxN = maxN;
        this.name = name;
    }

    public int getMaxN() {
        return maxN;
    }

    public String getName() {
        return name;
    }
}
