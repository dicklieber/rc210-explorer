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

    logicAlarmKey(5),

    meterKey(8),
    dtmfMacroKey(195, false),
    courtesyToneKey(10),
    functionKey(1005, false),
    macroKey(105),
    messageMacroKey(70),
    commonKey(1),
    wordKey(256, false),
    portKey(3),
    scheduleKey(40),
    timerKey(6);

    private final int MaxN;
    private boolean nameable = true;

    public boolean isNameable() {
        return nameable;
    }

    KeyKind(int maxN) {
        this.MaxN = maxN;

    }

    KeyKind(int maxN, boolean nameable) {
        this.MaxN = maxN;
        this.nameable = nameable;
    }


    public int maxN() {
        return MaxN;
    }

    public String skey(int number) {
        return toString() + number;
    }

}
