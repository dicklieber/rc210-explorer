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

package net.wa9nnn.rc210.serial.comm

import com.typesafe.config.Config

import java.time

/**
 * Non-UI controlled serial port stuff.
 *
 * @param readTimeoutMs how long to wait for a response from the RC-210.
 */
case class SerialConfig(readTimeoutMs: Int)

object SerialConfig:

  def apply(config: Config): SerialConfig =
    new SerialConfig(config.getDuration("vizRc210.serialReadTimeout").toMillis.toInt)

