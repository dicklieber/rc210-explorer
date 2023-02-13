package net.wa9nnn.rc210.data.schedules

import net.wa9nnn.rc210.fixtures.WithMemory

class ScheduleExtractorSpec extends WithMemory {

  "ScheduleExtractor" should {
    "apply" in {
      val schedules: Seq[Schedule] = ScheduleExtractor(memory)
      schedules must haveLength(40)
    }
  }
}
