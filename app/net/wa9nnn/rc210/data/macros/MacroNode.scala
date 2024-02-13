package net.wa9nnn.rc210.data.macros

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, KvTable, Row, Table, TableInACell, TableSection}
import net.wa9nnn.rc210.data.Dtmf.Dtmf
import net.wa9nnn.rc210.data.{EditHandler, Node}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.MessageNode.{form, keyKind}
import net.wa9nnn.rc210.data.field.{ComplexExtractor, ComplexFieldValue, FieldEntry, FieldEntryBase, FieldOffset, FieldValue}
import net.wa9nnn.rc210.Functions
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.EditFlowButtonCell
import net.wa9nnn.rc210.{FieldKey, Key, KeyKind}
import play.api.data.Forms.*
import play.api.data.{Form, FormError}
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.*
import play.twirl.api.Html
import views.html.{fieldIndex, flowChartButton, macroEditor, messageEditor}

import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec
import scala.xml.Elem

/**
 * This RC-210 Macro
 *
 * @param key       unique id for this macro.
 * @param functions that this macro invokes.
 * @param dtmf      that can invoke this macro.
 */
case class MacroNode(override val key: Key, functions: Seq[Key], dtmf: Option[Dtmf] = None) extends ComplexFieldValue() with Node:

  //  def enabled: Boolean = functions.nonEmpty

  //  override val commandStringValue: String = "*4002 10 * 162 * 187 * 122 * 347" // todo

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntryBase): Seq[String] = {
    val numbers: String = functions.map(_.rc210Value).mkString("*")
    val macroNumber: String = f"${key.rc210Value}%03d"
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
      s"1*2050$macroNumber$dtmf"
    )
  }

  override def displayHtml: String = functions.map(_.rc210Value).mkString(" ")

  override def toJsValue: JsValue = Json.toJson(this)

  override def table(fieldKey: FieldKey): Table = {
    throw new NotImplementedError() //todo
    //    val value: Seq[Row] = functions.map { functionKey =>
    //      val name = functionKey.rc210Value.toString
    //      Row(name, FunctionsProvider(functionKey).description): _*
    //    }
    //    KvTable(s"Macro ${key.keyWithName}",
    //      TableSection("Functions", value)
    //    )
  }

  override def toRow: Row = Row(
    EditFlowButtonCell(fieldKey),

    TableInACell(Table(Seq.empty,
      functions.map { fKey =>
        Row.ofAny(
          s"${Functions.description(fKey)} (${fKey.rc210Value})"
        )
      }).withCssClass("functionsTable")
    ))

object MacroNode extends ComplexExtractor[MacroNode]:
  override val keyKind: KeyKind = KeyKind.Macro

  def unapply(u: MacroNode): Option[(Key, Seq[Key], Option[Dtmf])] = Some((u.key, u.functions, u.dtmf))

  implicit val fmtMacro: Format[MacroNode] = Json.format[MacroNode]

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
          val key: Key = Key(KeyKind.Macro, mai.getAndIncrement())
          val sChunk = chunk.ints
            .map(_.toString)
            .mkString(", ")

          val functions: Seq[Key] = try {
            parseChunk(chunk.iterator)
          } catch {
            case e: NoSuchElementException =>
              logger.error(s"macroKey: $key Ran out in chunk: $sChunk assuming no functions!")
              Seq.empty
            case e: Exception =>
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
        Some(Key(KeyKind.Function, number))
      case 255 =>
        parseFunction(iterator, soFar + int) // add next int, which will always be 255.
    }
  }

  override def parse(jsValue: JsValue): FieldValue = {
    jsValue.as[MacroNode]
  }

  override def form: Form[MacroNode] = throw new NotImplementedError("Forms not used with macros") //todo

  override def index(values: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val topHeader = s"Macros  (${values.length})"
    val header = Header(topHeader,
      "",
      "Functions"
    )
    val table = Table(header,
      values.map(_.value.toRow)
    )
    fieldIndex(keyKind, table)

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    macroEditor(fieldEntry.value)

  override def bindFromRequest(using data: Map[String, Seq[String]]): Seq[UpdateCandidate] =

    (for {
      fieldKey <- EditHandler.fieldKey
      ids <- EditHandler.str("1,2,3")
    } yield {
      val strings: Array[String] = ids.split(',').filter(_.nonEmpty)
      val messageNode = MacroNode(fieldKey.key, strings.map(s => Key(KeyKind.Function, s.toInt)))
      UpdateCandidate(fieldKey, messageNode)
    }).toSeq





