package net.wa9nnn.rc210.data

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{FieldKey, KeyKind, NamedKey}
import net.wa9nnn.rc210.data.datastore.UpdateCandidate
import net.wa9nnn.rc210.data.field.FieldEntry
import play.api.i18n.MessagesProvider
import play.api.mvc.RequestHeader
import play.twirl.api.Html

import scala.collection.immutable

/**
 * Behavior for [[KeyKind]]s that are just a bunch of [[net.wa9nnn.rc210.data.field.SimpleFieldValue]]s.
 *
 * @param keyKind
 */
abstract class SimpleFieldNode(val keyKind: KeyKind) extends EditHandler with LazyLogging:
  def collect(data: Map[String, Seq[String]]): Seq[UpdateCandidate] =

    val valueSet: immutable.Iterable[UpdateCandidate] = (
      for {
        (id: String, values: Seq[String]) <- data
        if id != "fieldKey"
        if id != "key"
        fieldKey = FieldKey.fromId(id)
        if fieldKey.fieldName != NamedKey.fieldName
        str <- values.headOption
      } yield {
        UpdateCandidate(fieldKey, str)
      }
      ).toSeq

    valueSet.toSeq
