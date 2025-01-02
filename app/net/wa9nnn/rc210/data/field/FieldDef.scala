package net.wa9nnn.rc210.data.field

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.clock.ClockNode
import net.wa9nnn.rc210.data.datastore.{DataStore, UpdateCandidate}
import net.wa9nnn.rc210.serial.Memory
import net.wa9nnn.rc210.ui.FormData
import net.wa9nnn.rc210.{Key, KeyMetadata}
import play.api.data.Form
import play.api.i18n.MessagesProvider
import play.api.libs.json.*
import play.api.mvc.RequestHeader
import play.twirl.api.Html

import java.awt.MenuItem
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
  def fieldName: String
  def keyMetadata: KeyMetadata
  def fmt: Format[T]


trait FieldDefSimple[T <: FieldValue] extends FieldDef[T] :
  def offset: Int
  val template: String

  
  def fromString(str: String): T

  /**
   * Takes the next item from the iterator
   *
   * @param iterator
   * @return
   */
  def extract(iterator: Iterator[Int]): FieldValueSimple

trait FormSupport[T <: FieldValue]:
  def fromForm(formValue: String): T

trait MenuSupport extends MenuItem



