
package net.wa9nnn.rc210

import java.nio.file.{Files, Path}
import scala.io.BufferedSource


class DatFile(sections: Seq[DatSection]) {
  private val map: Map[String, DatSection] = sections.map(datSection => datSection.sectionName -> datSection).toMap

  def section(sectionName: String): DatSection = {
    map(sectionName)
  }

  def dump(): Unit = {
    sections.foreach(_.dump())
  }
}

object DatFileIo {
  private val header = """\[(.+)]""".r

  def read(file: Path): DatFile = {
    val inputStream = Files.newInputStream(file)
    val source = new BufferedSource(inputStream)
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
    val datSections: Seq[DatSection] = sectionsBuilder.result()
    new DatFile(datSections)

  }

}

case class DebugInfo(line: String, index: Int){
  override def toString: String = s"$index: $line"
}