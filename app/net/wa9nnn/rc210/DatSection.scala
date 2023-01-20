package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

case class DatSection(sectionName: String, dataItems: Seq[DataItem]) extends LazyLogging {
  def dump(): Unit = {
    logger.info("Section: {} ({} items)", sectionName, dataItems.size)
    dataItems.foreach {
      _.dump()
    }
  }
}

class SectionBuilder(name: String) extends LazyLogging {
  private val entriesBuilder = Seq.newBuilder[DataItem]

  def appendLine(dataLine: String, index:Int): Unit = {
    val debugInfo = DebugInfo(dataLine, index)

    DataItem(debugInfo) match {
      case Failure(exception) =>
        logger.error("Parsing line: {}", exception, dataLine)
      case Success(di) =>
        entriesBuilder += di
    }
  }

  def result: DatSection = DatSection(name, entriesBuilder.result())

}
