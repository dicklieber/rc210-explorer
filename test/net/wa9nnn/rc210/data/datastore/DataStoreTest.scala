package net.wa9nnn.rc210.data.datastore

import net.wa9nnn.rc210.data.field.{FieldDefinitions, FieldEntry}
import net.wa9nnn.rc210.security.authentication.{RcSession, User}
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind, WithTestConfiguration}

import scala.util.Failure

class DataStoreTest extends WithDataStore {



  "DataStore" when {
    /*    "FlowData" should {
        os.list(dataDirectory).foreach(println(_))
        //    val definitions: FieldDefinitions = new FieldDefinitions
        //    val memoryFileLoader: MemoryFileLoader = new MemoryFileLoader(definitions)
        //    val dataStorePersistence: DataStorePersistence = new DataStorePersistence()
        //    val dataStore = new DataStore(dataStorePersistence, memoryFileLoader)
        //    "Timer as trigger" in {
        val timerKey = Key.timerKeys.head
        val macroKey = Key.macroKeys(80)

        val timerNode = TimerNode(timerKey, 123, macroKey)
        val fieldKey = timerNode.fieldKey
        val dataStore = newDataStore
        dataStore.update(CandidateAndNames(UpdateCandidate(fieldKey, timerNode)))

        val fieldsTriggerByMacro: Seq[FieldEntry] = dataStore.triggerNodes(macroKey)
        fieldsTriggerByMacro.foreach { fieldEntry =>
          println(fieldEntry)
        }
      }
  */ "dump triggers" in {
      val dataStore = newDataStore

      val triggers: Seq[FieldEntry] = dataStore.triggers
      triggers.foreach(fieldEntry =>
        println(fieldEntry))
    }
    "update" when {
      "simple field" in {
        val dataStore = newDataStore
        val fieldKey = FieldKey(Key.commonkey, "Say Hours")
        val b4: FieldEntry = dataStore(fieldKey)
        val candidate = UpdateCandidate(fieldKey, "true")
        val candidateAndNames = CandidateAndNames(candidate)
        dataStore.update(candidateAndNames)
        val after = dataStore(fieldKey)
        val candidate1 = after.candidate
        candidate1

      }
    }
  }
}
