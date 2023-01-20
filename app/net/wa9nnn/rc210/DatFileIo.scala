
package net.wa9nnn.rc210

import org.apache.commons.io.IOUtils

import java.nio.file.{Files, Path}
import scala.io.{BufferedSource, Source}

/**
 * Internalized representation of a RCP .dat file
 * as a map of [[DatSection]]s.
 */
class DatFile(sections: Seq[DatSection]) {
  def size: Int = sections.size

  private val map: Map[String, DatSection] = sections.map(datSection => datSection.sectionName -> datSection).toMap

  def section(sectionName: String): DatSection = {
    map(sectionName)
  }
  def head:DatSection = sections.head

  def dump(): Unit = {
    sections.foreach(_.dump())
  }
}

object DatFileIo {
  private val header = """\[(.+)]""".r

  def apply(file: Path): DatFile = {
    apply(new BufferedSource(Files.newInputStream(file)))
  }

  def apply(string: String): DatFile = {
    apply(new BufferedSource(IOUtils.toInputStream(string, "UtF-8")))
  }

  def apply(source: Source): DatFile = {
    val sectionsBuilder = Seq.newBuilder[DatSection]
    var currentSectionBuilder: Option[SectionBuilder] = None

    source.getLines()
      .zipWithIndex
      .foreach { case (line, index) =>
        line match {
          case header(sectName) =>
            currentSectionBuilder.foreach { sb =>
              sectionsBuilder += sb.result
            }
            currentSectionBuilder = Option(new SectionBuilder(sectName))

          case "" =>
          // ignore empty line

          case dataLine: String =>
            currentSectionBuilder.get.appendLine(dataLine, index)
        }
      }
    currentSectionBuilder.foreach { sb =>
      sectionsBuilder += sb.result
    }

    val datSections: Seq[DatSection] = sectionsBuilder.result()
    new DatFile(datSections)
  }
}


case class RawDataLine(line: String, index: Int) {
  override def toString: String = s"$index: $line"
}