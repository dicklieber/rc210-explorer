package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{Key, KeyMetadata, NamedKey}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.ui.FormData
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

    val valueSet: immutable.Iterable[UpdateCandidate] = (
      for {
        (id: String, values: Seq[String]) <- formData.map
        if id != "fieldKey"
        if id != "key"
        key = Key.fromId(id)
        if key.qualifier.contains( NamedKey.fieldName)
        str <- values.headOption
      } yield {
        UpdateCandidate(key, candidate = str)
      }
      ).toSeq

    valueSet.toSeq
