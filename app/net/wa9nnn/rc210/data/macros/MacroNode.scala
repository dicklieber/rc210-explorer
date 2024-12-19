package net.wa9nnn.rc210.data.macros

import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Row, Table, TableInACell}
import net.wa9nnn.rc210.Functions.functions
import net.wa9nnn.rc210.data.Dtmf.Dtmf
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.data.{EditHandler, Node}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.ButtonCell
import net.wa9nnn.rc210.{FieldKey, FunctionNode, Functions, Key, KeyKind}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.*
import play.twirl.api.Html
import views.html.{fieldIndex, macroEditor}

import java.time.temporal.TemporalQueries.offset
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec

/**
 * This RC-210 Macro
 *
 * @param key       unique id for this macro.
 * @param functions that this macro invokes.
 * @param dtmf      that can invoke this macro.
 */
case class MacroNode(override val key: Key, functions: Seq[Key], dtmf: Option[Dtmf] = None) extends ComplexFieldValue() with Node:
  override def toString: String =
    val seq1: Seq[Any] = functions.map(_.rc210Value)
    val sFunctions:String = seq1.mkString(" ")
      s"$key: dtmf: ${dtmf.getOrElse("")} functions=$sFunctions"

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

  override def displayCell: Cell = Cell(functions.map(_.rc210Value).mkString(" "))

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
    ButtonCell.edit(fieldKey),
    fieldKey.key.keyWithName,
    dtmf,
    TableInACell(Table(Seq.empty,
      functions.map { fKey =>
        Row.ofAny(
          s"${Functions.description(fKey)} (${fKey.rc210Value})"
        )
      }).withCssClass("functionsTable")
    ))



object MacroNode extends ComplexFieldDefinition[MacroNode]:
  override val keyKind: KeyKind = KeyKind.Macro

  def unapply(u: MacroNode): Option[(Key, Seq[Key], Option[Dtmf])] = Some((u.key, u.functions, u.dtmf))

  implicit val fmtMacro: Format[MacroNode] = Json.format[MacroNode]

  override def positions: Seq[FieldOffset] = Seq(
    FieldOffset(1985, this),
    FieldOffset(2825, this),
  )

  private val longMacroSlots = 15
  private val shortMacroSlots = 4

  override def extract(memory: Memory): Seq[FieldEntry] =

    val dtmfMacros: DtmfMacros = DtmfMacroExtractor(memory)
    val mai = new AtomicInteger(1)

    def readMacros(offset: Int, nMacros:Int, functionLength:Int): Seq[FieldEntry] =
      val chunks = memory.chunks(offset, functionLength + 1,nMacros)
      chunks.map { chunk =>
        val key: Key = Key(KeyKind.Macro, mai.getAndIncrement())
        val functions: Iterable[Key] = chunk.map { functionNumber =>
          Key.apply(KeyKind.Function, functionNumber)
        }.takeWhile(_.rc210Value != 0)
        val macroNode: MacroNode = MacroNode(key, functions.toSeq, dtmfMacros(key))
        FieldEntry(this, FieldKey(key), macroNode)
      }

    val longMacroNodes: Seq[FieldEntry] = readMacros(1985, 40, longMacroSlots)
    val shortMacroNodes: Seq[FieldEntry] = readMacros(2825, 50, shortMacroSlots)
    val entries = longMacroNodes ++ shortMacroNodes
    entries


  override def parse(jsValue: JsValue): FieldValue = {
    jsValue.as[MacroNode]
  }

  override def form: Form[MacroNode] = throw new NotImplementedError("Forms not used with macros") //todo

  override def index(fieldEntries: Seq[FieldEntry])(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    val topHeader = s"Macros  (${fieldEntries.length})"
    val header = Header(topHeader,
      "",
      "Key",
      "DTMF",
      "Functions"
    )
    val table = Table(header,
      fieldEntries.map(_.value.toRow)
    )
    fieldIndex(keyKind, table)

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    macroEditor(fieldEntry.value)

  override def bind(using data: Map[String, Seq[String]]): Seq[UpdateCandidate] =

    (for {
      fieldKey <- EditHandler.fieldKey
      ids <- EditHandler.str("ids")
    } yield {
      val strings: Array[String] = ids.split(',').filter(_.nonEmpty)
      val functions: Seq[Key] = strings.toIndexedSeq.map(s => Key(KeyKind.Function, s.toInt))
      val messageNode = MacroNode(fieldKey.key,
        functions,
        EditHandler.str("dtmf")
      )
      UpdateCandidate(fieldKey, messageNode)
    }).toSeq





