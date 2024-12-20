package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{Key, KeyMetadata, NamedKey}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.FieldEntry
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
  def collect(data: Map[String, Seq[String]]): Seq[UpdateCandidate] =

    val valueSet: immutable.Iterable[UpdateCandidate] = (
      for {
        (id: String, values: Seq[String]) <- data
        if id != "fieldKey"
        if id != "key"
        key = Key.fromId(id)
        if key.fieldName != NamedKey.fieldName
        str <- values.headOption
      } yield {
        UpdateCandidate(candidate = str)
      }
      ).toSeq

    valueSet.toSeq
