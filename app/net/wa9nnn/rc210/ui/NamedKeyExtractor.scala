package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.{FieldKey, NamedKey}

import scala.collection.immutable

object NamedKeyExtractor:
  /**
   * Get all the [[NamedKey]] data from the submitted form.
   *
   * @param data submitted from data fields.
   * @return zero or more named keys.
   */
  def apply(data: Map[String, Seq[String]]): Seq[NamedKey] =
    (for
      tuple <- data
      id: String = tuple._1
      if id.endsWith(NamedKey.fieldName)
      values = tuple._2
      keyName <- values.headOption
    yield
      val fieldKey = FieldKey.fromId(id)
      NamedKey(fieldKey.key, keyName)
  ).toSeq

