package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.clock.ClockNode
import net.wa9nnn.rc210.data.datastore.{DataStore, KeyAndValue, UpdateCandidate}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.{FormData, MenuItem}
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.data.Form
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, JsObject, JsResult, JsValue, Json, OFormat, Reads}
import play.api.mvc.RequestHeader
import play.twirl.api.Html

import scala.util.Try

/**
 * Knows how to:
 * - extract current values from [[Memory]] (data from RC-210).
 * - provides Play Json Format [[fmt]] to read or write JSON.
 * - instantiate a [[FieldValue]] from an HTML form. [[fromForm()]]
 * - provides a [[KeyMetadata]] to help create [[Key]]s for
 *
 * @tparam T what this produces.
 */
trait FieldDef[T <: FieldValue] extends LazyLogging:
  val fieldName: String
  val keyMetadata:KeyMetadata
  val template: String = ""
  val fmt: Format[T]


//  def positions: Seq[FieldOffset]

//  def fromForm(formData: FormData):Seq[UpdateCandidate]
//  def fromString(string: String): Try[UpdateCandidate]
//
//  override def writes(o: FieldEntry): JsObject = throw new NotImplementedError(s"No JSON for ${o.fieldDefinition.fieldName}") //todo
//
//  override def reads(json: JsValue): JsResult[FieldEntry] = throw new NotImplementedError(s"No JSON for $json")

//abstract class FieldDefSimple[T<:FieldValue] extends FieldExtractor :
//  val fmt: Format[FieldValue]
//
//  def extractFromInts(iterator: Iterator[Int], fieldDefinition: FieldDefSimple): FieldValue
//
//  def update(formFieldValue: String): FieldValueSimple
//


trait FieldDefSimple[T <: FieldValue] extends FieldDef
  with JsonSupport[T]
  val offset:Int
  def fromString(str:String):T

  /**
   * Takes the next item from the iterator
   * @param iterator
   * @return
   */
  def extract(iterator: Iterator[Int]): FieldValueSimple
  
trait JsonSupport[T <: FieldValue] extends OFormat[T]

trait FormSupport[T <: FieldValue]:
  def fromForm(formValue: String): T


trait MenuSupport extends MenuItem

trait EditSupport extends MenuSupport:
  /**
   * Extract fields from the [[DataStore]] and build top-level page. Either a list of items
   * of the edit page for Common or Ports.
   *
   * @return the page.
   */
  def index(dataStore: DataStore)(using request: RequestHeader, messagesProvider: MessagesProvider): Html

  /**
   * Edit one [[FieldEntry]]
   *
   * @param fieldEntry from the [[net.wa9nnn.rc210.data.datastore.DataStore]].
   * @return
   */
  def edit(fieldEntry: FieldEntry)(using request: RequestHeader, messagesProvider: MessagesProvider): Html

  def bind(formData: FormData): Iterable[UpdateCandidate] =
    throw new NotImplementedError()

