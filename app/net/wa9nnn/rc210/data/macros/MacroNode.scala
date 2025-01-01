package net.wa9nnn.rc210.data.macros

import com.wa9nnn.wa9nnnutil.tableui.*
import net.wa9nnn.rc210.data.Dtmf.Dtmf
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.*
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.{ButtonCell, FormData}
import net.wa9nnn.rc210.{FunctionNode, Functions, Key, KeyMetadata}
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
case class MacroNode(functions: Seq[Key], dtmf: Option[Dtmf] = None)
  extends FieldValueComplex[MacroNode]():
  override def toRow(key: Key): Row =
    Row(
      ButtonCell.edit(key),
      key.keyWithName,
      dtmf,
      TableInACell(Table(Header.none,
        functions.map { fKey =>
          Row.ofAny(
            s"${Functions.description(fKey)} (${fKey.rc210Number})"
          ).withCssClass("functionsTable")
        }
      ))
    )

  override def toString: String =
    val seq1: Seq[Any] = functions.map(_.rc210Number)
    val sFunctions: String = seq1.mkString(" ")
    s"dtmf: ${dtmf.getOrElse("")} functions=$sFunctions"

  /**
   * Render this value as an RD-210 command string.
   */
  override def toCommands(fieldEntry: FieldEntry): Seq[String] =
    val key = fieldEntry.key
    val numbers: String = functions.map(_.rc210Number).mkString("*")
    val macroNumber: String = f"${key.rc210Number.get}%03d"
    val mCmd = if functions.isEmpty then
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

  override def displayCell: Cell =
    TableInACell(Table(Header.none,
      for
        fkey <- functions
        functionNode <- Functions.maybeFunctionNode(fkey)
      yield
        Row(
          functionNode.description,
          functionNode.key.rc210Number,
          functionNode.destination
        )
    ))

object MacroNode extends FieldDefComplex[MacroNode]:
  override val keyMetadata: KeyMetadata = KeyMetadata.Macro
  val idsKey: Key = Key(KeyMetadata.Macro, "ids")
  val dtmfKey: Key = Key(KeyMetadata.Macro, "dtmf")

  def unapply(u: MacroNode): Option[(Seq[Key], Option[Dtmf])] = Some((u.functions, u.dtmf))

  implicit val fmt: Format[MacroNode] = Json.format[MacroNode]
  
  private val longMacroSlots = 15
  private val shortMacroSlots = 4

  override def extract(memory: Memory): Seq[FieldEntry] =

    val dtmfMacros: DtmfMacros = DtmfMacroExtractor(memory)
    val mai = new AtomicInteger(1)

    def readMacros(offset: Int, nMacros: Int, functionLength: Int): Seq[FieldEntry] =
      val chunks = memory.chunks(offset, functionLength + 1, nMacros)
      chunks.map { chunk =>
        val key: Key = Key(KeyMetadata.Macro, mai.getAndIncrement())
        val functions: Iterable[Key] = chunk.map { functionNumber =>
          Key(KeyMetadata.Function, Option(functionNumber))
        }.takeWhile(_.rc210Number.get != 0)
        val macroNode: MacroNode = MacroNode(functions.toSeq, dtmfMacros(key))
        FieldEntry(this, key, macroNode)
      }

    val longMacroNodes: Seq[FieldEntry] = readMacros(1985, 40, longMacroSlots)
    val shortMacroNodes: Seq[FieldEntry] = readMacros(2825, 50, shortMacroSlots)
    val entries = longMacroNodes ++ shortMacroNodes
    entries

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
      fieldEntries.map(fe =>
      {
        val value: MacroNode = fe.value
        value.toRow(fe.key)
      })
    )
    fieldIndex(keyMetadata, table)

  override def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html =
    macroEditor(fieldEntry.key, fieldEntry.value)

  override def bind(formData: FormData): Iterable[UpdateCandidate] =
    for
      key <- formData.maybeKey
    yield
      val strings: Array[String] = formData.value("ids").split(',').filter(_.nonEmpty)
      val functions: Seq[Key] = strings.toIndexedSeq.map(s => Key(KeyMetadata.Function, s.toInt))

      val macroNode = MacroNode(
        functions,
        formData.valueOpt("dtmf")
      )
      UpdateCandidate(key, macroNode)





