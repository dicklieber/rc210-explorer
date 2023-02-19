package net.wa9nnn.rc210.data.schedules

import net.wa9nnn.rc210.data.Rc210Data
import net.wa9nnn.rc210.fixtures.WithMemory

class ScheduleExtractorSpec extends WithMemory {

  "ScheduleExtractor" should {
    "apply" in {
      val se = new ScheduleExtractor()
      val rc210Data = se(memory, Rc210Data())
      rc210Data.schedules must haveLength(40)
    }
  }
}
