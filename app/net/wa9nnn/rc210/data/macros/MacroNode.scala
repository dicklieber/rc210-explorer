package net.wa9nnn.rc210.data.macros

import com.wa9nnn.util.tableui.{Header, Row}
import net.wa9nnn.rc210.data.{Dtmf, TriggerNode}
import net.wa9nnn.rc210.{Key, KeyKind}
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue, FieldEntry, FieldEntryBase, FieldOffset, FieldValue}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.FormFields
import play.api.libs.json.{Format, JsValue, Json}

import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec

/**
 *
 * @param key       unique id for this macro.
 * @param functions that this macro invokes.
 * @param dtmf      that can invoke this macro.
 */
case class MacroNode(override val key: Key , functions: Seq[Key], dtmf: Option[Dtmf] = None) extends ComplexFieldValue("Macro") with TriggerNode {

  def enabled: Boolean = functions.nonEmpty

  override def canRunMacro(macroKey: Key): Boolean = false //todo look for FunctionNode with destination that has the macro of interest.

  //  override val commandStringValue: String = "*4002 10 * 162 * 187 * 122 * 347" // todo


  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val numbers: String = functions.map(_.rc210Value).mkString("*")
    val macroNumber: String = f"${key.rc210Value}%03d"
    val dtmfPart: String = dtmf.map(_.value).getOrElse("")
    val mCmd = if (functions.isEmpty)
      s"1*4003$macroNumber" // erase macro
    else
      s"1*4002$macroNumber*$numbers"

    //    1*4002 11 * 118 * 391 ok
    //    1*400211 * 118 * 391 ok
    //    1*400211*118*391 ok
    //    1*4002011*118*391 ok

    Seq(
      mCmd,
      s"1*2050$macroNumber$dtmfPart"
    )
  }


  override def toRow: Row = {
    throw new NotImplementedError() //todo
  }

  override def display: String = functions.map(_.rc210Value).mkString(" ")

  override def toJsonValue: JsValue = Json.toJson(this)


}

object MacroNode extends ComplexExtractor {
  def header(count: Int): Header = Header(s"Macros ($count)", "Key", "Functions")

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(1985, this),
    FieldOffset(2825, this),
  )

  override def extract(memory: Memory): Seq[FieldEntry] = {

    val dtmfMacros: DtmfMacros = DtmfMacroExtractor(memory)
    val mai = new AtomicInteger(1)

    def macroBuilder(offset: Int, chunkLength: Int, nChunks: Int) = {
      memory.chunks(offset, chunkLength, nChunks)
        .map { chunk =>
          val key: Key = Key(KeyKind.macroKey, mai.getAndIncrement())
          val sChunk = chunk.ints
            .map(_.toString)
            .mkString(", ")


          val functions: Seq[Key] = try {
            parseChunk(chunk.iterator)
          } catch {
            case e: NoSuchElementException =>
              logger.error(s"macroKey: $key Ran out in chunk: $sChunk assuming no functions!")
              Seq.empty
            case e:Exception =>
              logger.error(s"macroKey: $key Error parsing chunk: $sChunk ${e.getMessage}")
              Seq.empty

          }
          MacroNode(key, functions, dtmfMacros(key))
        }
    }

    val macros: Seq[MacroNode] = macroBuilder(1985, 16, 40) //SlicePos("//Macro - 1985-2624"), memory, 16)
      .concat(macroBuilder(2825, 7, 50)) // SlicePos("//ShortMacro - 2825-3174"), memory, 7))

    val r: Seq[FieldEntry] = macros.map { m =>
      FieldEntry(this, m.fieldKey, m)
    }
    r
  }

  /**
   * Get all the Function key from a chunk of RC_210-210 data.
   *
   * @param iterator of the chunk.
   * @return
   */
  @tailrec
  def parseChunk(iterator: Iterator[Int], soFar: Seq[Key] = Seq.empty): Seq[Key] = {
    parseFunction(iterator) match {
      case Some(functionKey) =>
        parseChunk(iterator, soFar :+ functionKey)
      case None =>
        soFar
    }
  }

  /**
   * Accumulate functionKey frpm ints. Handle 255s and less than 255
   *
   * @param iterator within the chunk.
   * @return
   */
  @tailrec
  def parseFunction(iterator: Iterator[Int], soFar: Int = 0): Option[Key] = {
    val int = iterator.next()
    int match {
      case 0 =>
        None // Done. function numbers don't allow 255,512,767.
      case x: Int if x < 255 =>
        val number = soFar + x
        Some(Key(KeyKind.functionKey, number))
      case 255 =>
        parseFunction(iterator, soFar + int) // add next int, which will always be 255.
    }
  }

  implicit val fmtMacroNode: Format[MacroNode] = Json.format[MacroNode]

  override def parse(jsValue: JsValue): FieldValue = {
    jsValue.as[MacroNode]
  }

  override val name: String = "MacroNode"
  override val fieldName: String = name
  override val kind: KeyKind = KeyKind.macroKey

  override def parseForm(formFields: FormFields): ComplexFieldValue = ???
}


