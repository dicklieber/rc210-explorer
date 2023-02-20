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

package net.wa9nnn.rc210.data.duration


import io.circe
import io.circe.generic.JsonCodec
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}

import java.time.Duration

/**
 * RC-210 data has a variety of durations. They are all handled as integers but
 * the units vary e.g. minutes, seconds, tenths of seconds etc.
 * We have a specific type for each kind.  This allows the UI to determine how to interact with the user
 * based on the type. The underlying type is a java [[Duration]], which makes display the actual duration to the user easy.
 */
sealed trait RCDuration {
  def duration: Duration

  /**
   * RC-210 using integers for various durations.
   *
   * @return
   */
  def toInteger: Long
}

case class Minutes(duration: Duration) extends RCDuration {
  /**
   * based on the unit of each type.
   *
   * @return
   */
  override def toInteger: Long = duration.toMinutes
}

object Minutes {
  def apply(minutes: String): Minutes = {
    new Minutes(Duration.ofMinutes(minutes.toLong))
  }

  implicit val encodeSeconds: Encoder[Minutes] = new Encoder[Minutes] {
    final def apply(a: Minutes): Json = Json.fromString(
      a.duration.toString
    )
  }

  implicit val decodeSeconds: Decoder[Minutes] = new Decoder[Minutes] {
    final def apply(c: HCursor): Decoder.Result[Minutes] = {
      Right(new Minutes(Duration.parse(c.value.asString.get)))
    }
  }
}

case class Seconds(duration: Duration) extends RCDuration {
  /**
   * based on the unit of each type.
   *
   * @return
   */
  override def toInteger: Long = duration.toSeconds
}

object Seconds {
  def apply(seconds: String): Seconds = {
    new Seconds(Duration.ofSeconds(seconds.toLong))
  }

  implicit val encodeSeconds: Encoder[Seconds] = new Encoder[Seconds] {
    final def apply(a: Seconds): Json = Json.fromString(
      a.duration.toString
    )
  }

  implicit val decodeSeconds: Decoder[Seconds] = new Decoder[Seconds] {
    final def apply(c: HCursor): Decoder.Result[Seconds] = {
      Right(new Seconds(Duration.parse(c.value.asString.get)))
    }
  }
}

case class TenthSeconds(duration: Duration) extends RCDuration {
  /**
   * based on the unit of each type.
   *
   * @return
   */
  override def toInteger: Long = duration.toMillis / 10
}

object TenthSeconds {
  def apply(tenths: Int): TenthSeconds = new TenthSeconds(Duration.ofMillis(tenths * 100))

  def apply(tenths: String): TenthSeconds = new TenthSeconds(Duration.ofMillis(tenths.toInt * 100))

  implicit val encodeSeconds: Encoder[TenthSeconds] = (a: TenthSeconds) => Json.fromString(
    a.duration.toString
  )

  implicit val decodeSeconds: Decoder[TenthSeconds] = (c: HCursor) => {
    Right(TenthSeconds(Duration.parse(c.value.asString.get)))
  }
}

case class Milliseconds(duration: Duration) extends RCDuration {
  /**
   * based on the unit of each type.
   *
   * @return
   */
  override def toInteger: Long = duration.toMillis
}
object Milliseconds {
  def apply(millis: Int): Milliseconds = new Milliseconds(Duration.ofMillis(millis))

  def apply(tenths: String): Milliseconds = new Milliseconds(Duration.ofMillis(tenths.toLong))

  implicit val encodeSeconds: Encoder[Milliseconds] = (a: Milliseconds) => Json.fromString(
    a.duration.toString
  )

  implicit val decodeSeconds: Decoder[Milliseconds] = (c: HCursor) => {
    Right(Milliseconds(Duration.parse(c.value.asString.get)))
  }
}


object DurationPlayPen extends App {

  @JsonCodec
  case class Things(i: Int, seconds: Seconds)

  val thing = Things(123, Seconds("12"))
  private val thingJson: Json = thing.asJson

  println(s"thingJson $thingJson")

  private val sThingJson: String = thingJson.toString
  private val thEither: Either[circe.Error, Things] = decode[Things](sThingJson)
  thEither match {
    case Left(error) =>
      error.printStackTrace()

    case Right(th) =>
      println(s"backAgain: $th")

  }


}