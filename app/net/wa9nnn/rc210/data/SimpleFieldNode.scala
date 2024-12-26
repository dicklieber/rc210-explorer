package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{Key, KeyMetadata, NamedKey}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.ui.{FormData, KeyAndValues}
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.Html

import scala.collection.immutable

/**
 * Behavior for [[KeyMetadata]]s that are just a bunch of [[net.wa9nnn.rc210.data.field.FieldValueSimple]]s.
 *
 * @param keyKind
 */
abstract class SimpleFieldNode(val keyKind: KeyMetadata) extends EditHandler with LazyLogging:
  def collect(formData: FormData): Seq[UpdateCandidate] =
    val data = formData.data
    throw new NotImplementedError() //todo
//    UpdateCandidate(kk)


