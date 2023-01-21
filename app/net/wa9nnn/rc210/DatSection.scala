package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.model.DataItem

/**
 * One section of an RCP .dat file.
 *
 * @param sectionName  from [sectionName]
 * @param dataItems    lines upto next section or end ofd the file.
 */
case class DatSection private(sectionName: String, dataItems: Seq[DataItem]) extends LazyLogging {
  def dump(): Unit = {
    logger.info("Section: {} ({} items)", sectionName, dataItems.size)
    dataItems.foreach {
      _.dump()
    }
  }
}

class SectionBuilder(name: String) extends LazyLogging {
  private val entriesBuilder = Seq.newBuilder[DataItem]

  def appendLine(dataLine: String, index: Int): Unit = {
    val debugInfo = RawDataLine(dataLine, index)
    entriesBuilder += DataItem(debugInfo)
  }

  def result: DatSection = DatSection(name, entriesBuilder.result())
}