package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.{Memory, SlicePos}
import play.api.libs.json.{Json, OFormat}

import scala.math.Ordered.orderingToOrdered

object Commands extends LazyLogging {

  val commandSpecs: Seq[CommandSpecBase] = Seq(
    DTMFSpec("SitePrefix", "*2108", SlicePos(0, 4)),
    DTMFSpec("TTPadTest", "*2093", SlicePos(5, 6)),
    BoolSpec("SayHours", "*5104", 10),
    Hangtime(11),
  )

  //    CommandItem(DtmfParser, "SitePrefix", CommandId("*2108"), Slice(0,4),
  //    CommandItem(DtmfParser, "TTPadTest", CommandId("*2093"), Slice(5,6)),
  //    CommandItem(BooleanParser, "SayHours", CommandId("*5104"), slicer(1)),
  //    CommandItem(DtmfParser, "HangTime1", "*1000", slicer(3)),


  /**
   * Build RC-210 State i.e. [[ItemValue]]s from a [[Memory]].
   * This extracts all the [[ItemValue]]s from a [[Memory]].
   *
   * @param memory data freom an RC=210.
   * @return map of all parsaed values.
   */
  def parse(memory: Memory): Map[CommandId, ItemValue] = {
    (for {
      commandSpec: CommandSpecBase <- commandSpecs
      pr: ParseResult = commandSpec.parse(memory)
      itemValue <- pr.values
    } yield {
      itemValue.commandId -> itemValue
    }).toMap

  }


  type Command = String
}

