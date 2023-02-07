package net.wa9nnn.rc210.command

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.serial.{Memory, SlicePos}

object Commands extends LazyLogging {

/*
  val commandSpecs: Seq[CommandSpecBase] = Seq(
    DTMF("Site Prefix", "*2108", SlicePos(0, 4)),
    DTMF("TT Pad Test", "*2093", SlicePos(5, 6)),
    BoolSpec("Say Hours", "*5104", 10),
    Hangtime(11),
    PortInts("Initial ID Timer", "*1002", 20),
    PortInts("Pending ID Timer", "*1003", 23),
    PortInts("Tx Enable", "111", 26), // becomes p111v p=port v bool as 0 or 1
    PortInts("DTMF Covertone", "113", 29),
    PortInts16("DTMF Mute Timer", "*1006", 32),
    PortInts("Kerchunk ON/OFF", "115", 38),
    PortInts16("Kerchunk Timer", "*1008", 41),
    IntValue("Mute Digit Select", "*2090", 47),
    PortInts("CTCSS During ID", "*2089", 48),
    PortInts("CTCSS CT Control", "*2088", 51),
    IntValue("TimeoutPorts", "*2051", 54),
    IntValue16("Speech Delay", "*1019", 51),
    PortInts("CTCSS Encode Polarity", "*1021", 57),
    PortInts16("Guest Macro Range", "*4009", 60),
    PortInts("DTMF COS Control", "22", 67),
    PortInts("DTMF Enable", "116", 70),
    PortInts("DTMF Require Tone", "117", 73),
  )
*/

//  /**
//   * Build RC-210 State i.e. [[ItemValue]]s from a [[Memory]].
//   * This extracts all the [[ItemValue]]s from a [[Memory]].
//   *
//   * @param memory data freom an RC=210.
//   * @return map of all parsaed values.
//   */
//  def parse(memory: Memory): Map[CommandId, ItemValue] = {
//    (for {
//      commandSpec: CommandSpecBase <- commandSpecs
//      pr: ParseSliceResult = commandSpec.parse(memory)
//      itemValue <- pr.values
//    } yield {
//      itemValue.commandId -> itemValue
//    }).toMap
//
//  }

  type Command = String
}

