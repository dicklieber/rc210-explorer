package net.wa9nnn.rc210.data

import net.wa9nnn.rc210.DateFileParser
import net.wa9nnn.rc210.model.DatFile

import java.nio.file.Paths
import java.util.UUID
import javax.inject.Singleton

@Singleton
class DatFileSource {

  private val url1 = getClass.getResource("/examples/schedExamples.dat")
  private val path = Paths.get(url1.toURI)
  private val datFile: DatFile = DateFileParser(path)

  def datFile(sessionId: UUID =  UUID.randomUUID()): DatFile = {
    datFile
  }
}
