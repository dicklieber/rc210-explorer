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

import java.io.{File, PrintWriter}
import scala.util.Random

object HtmlEmit extends App {
  val r = new Random()
  case class Robot(name: String, strength: Int)
  val robots = (1 to 10).map(n => Robot(s"Robot $n", r.nextInt(101)))
  val p = new PrintWriter(new File("/tmp/robots.html"))

  p.println("<!DOCTYPE html>")
  p.println(
    <html>
      <head></head>
      <body>
        <h1>Robot Information</h1>
        <table>
          <thead>
            <tr>
              <td>Name</td>
              <td>Strength</td>
            </tr>
          </thead>
          <tbody>
            {robots.map(r => {
            <tr>
              <td>{r.name}</td>
              <td>{r.strength}</td>
            </tr>
          })}
          </tbody>
        </table>
      </body>
    </html>
  )
  p.close()
}