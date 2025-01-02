package net.wa9nnn.rc210

class KeyKindTest extends RcSpec:

  "KeyKindTest" when {
    "enumeratum.EnumEntry.CapitalWords" should {
      "withName" should {
        val meterAlarm = KeyMetadata.MeterAlarm
        "entryName" in {
          meterAlarm.entryName mustBe ("Meter Alarm")
        }
        "toString" in {
          meterAlarm.toString mustBe ("MeterAlarm")
        }
      }
    }
  }