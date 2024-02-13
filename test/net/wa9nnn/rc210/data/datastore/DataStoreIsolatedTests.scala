package net.wa9nnn.rc210.data.datastore

import net.wa9nnn.rc210.Key
import net.wa9nnn.rc210.{WithMemory, WithTestConfiguration}
import net.wa9nnn.rc210.data.field.{FieldDefinitions, FieldEntry}
import net.wa9nnn.rc210.data.timers.TimerNode
import net.wa9nnn.rc210.security.authentication.{RcSession, User}

class DataStoreIsolatedTests extends WithTestConfiguration {
  given RcSession = {
    val user = User(callsign = "WA9NNN", hash = "xyzzy")
    RcSession("foxtrot", user, "127.0.0.1")
  }

  "DataStore" when {
    "FlowData" should {
      val definitions: FieldDefinitions = new FieldDefinitions
      val memoryFileLoader: MemoryFileLoader = new MemoryFileLoader(definitions)
      val dataStorePersistence: DataStorePersistence = new DataStorePersistence()
      val dataStore = new DataStore(dataStorePersistence, memoryFileLoader)
      "Timer as trigger" in {
        val timerKey = Key.timerKeys.head
        val macroKey = Key.macroKeys(80)
  
        val timerNode = TimerNode(timerKey, 123, macroKey)
        val fieldKey = timerNode.fieldKey
        dataStore.update(CandidateAndNames(UpdateCandidate(fieldKey, timerNode)))

        val fieldsTriggerByMacro: Seq[FieldEntry] = dataStore.triggerNodes(macroKey)
        fieldsTriggerByMacro .foreach{ fieldEntry =>
          println(fieldEntry)
        }
        
        
        

      }
      "dump triggers" in {
        val triggers: Seq[FieldEntry] = dataStore.triggers
        triggers.foreach(fieldEntry =>
          println(fieldEntry))
      }
    }
  }
}
