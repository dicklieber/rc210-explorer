package net.wa9nnn.rc210.ui

import net.wa9nnn.rc210.{FieldKey, NamedKey}

object NamedKeyExtractor:
  /**
   * Get all the [[NamedKey]] data from the submitted form.
   *
   * @param data submitted from data fields.
   * @return zero or more named keys.
   */
  def apply(data: Map[String, Seq[String]]): Seq[NamedKey] =
    val namedKeys: Seq[NamedKey] = (for {
      (inputName: String, values: Seq[String]) <- data
      if inputName.endsWith(NamedKey.fieldName)
      fieldKey = FieldKey(inputName)
      name <- values.headOption
    } yield {
      NamedKey(fieldKey.key, name)
    }).toSeq
    namedKeys