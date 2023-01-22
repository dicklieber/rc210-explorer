
package net.wa9nnn.rc210

import net.wa9nnn.rc210.model.DatFile
import org.apache.commons.io.IOUtils

import java.nio.file.{Files, Path}
import scala.io.{BufferedSource, Source}

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