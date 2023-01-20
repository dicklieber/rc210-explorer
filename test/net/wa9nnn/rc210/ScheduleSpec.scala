package net.wa9nnn.rc210

import org.specs2.mutable.Specification

class ScheduleSpec extends Specification {

  val items = """DOW(3)=0
                |MonthToRun(3)=0
                |Monthly(3)=0
                |Week(3)=1
                |Hours(3)=07
                |Minutes(3)=00
                |MacroToRun(3)=03
                |""".stripMargin

  "ScheduleSpec" should {



    "buildSchdule" in {
      ok
    }
  }
}
