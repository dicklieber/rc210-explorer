package net.wa9nnn.rc210.data.timers

import net.wa9nnn.rc210.{Key, RcSpec}

import scala.language.postfixOps

class TimerNodeTest extends RcSpec {
  "TimerNode" when {
    val triggeredMacroKey = Key.macroKeys(3)
    val timerNode = TimerNode(Key.timerKeys.head, 42, triggeredMacroKey)
    "fieldKey" should {
      val fieldKey = timerNode.fieldKey
      "string" in {
        fieldKey.toString mustBe ("Timer1:Timer")
      }
    }

    "canRunMacro" should {
      "match Macro" in {
        timerNode.canRunMacro(triggeredMacroKey) mustBe true
      }
      "not match Macro" in {
        timerNode.canRunMacro(Key.macroKeys.head) mustBe false
      }
    }
  }
}
