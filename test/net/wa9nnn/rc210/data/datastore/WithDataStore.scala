package net.wa9nnn.rc210.data.datastore

import net.wa9nnn.rc210.{Key, WithTestConfiguration}
import net.wa9nnn.rc210.data.field.FieldDefinitions
import net.wa9nnn.rc210.security.authentication.{RcSession, User}

import scala.util.Failure

trait WithDataStore extends WithTestConfiguration {
  given RcSession = {
    val user = User(callsign = "WA9NNN", hash = "xyzzy")
    RcSession("foxtrot", user, "127.0.0.1")
  }

  /**
   * Instantiate a new [[DataStore]] with default data for testing.
   * @return
   */
  def newDataStore: DataStore = {
    val definitions: FieldDefinitions = new FieldDefinitions
    val memoryFileLoader: MemoryFileLoader = new MemoryFileLoader(definitions)
//     new DataStore()
     throw new NotImplementedError() //todo
//      def load() =
//        Failure(new NotImplementedError())
//
///*
//      def save(dataTransferJson: DataTransferJson): Unit =
//        println(dataTransferJson)
//*/
//    new DataStore(dataStorePersistence, memoryFileLoader)
  }
}
