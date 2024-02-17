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
    val namedKeys: Seq[NamedKey] = data.flatMap { fieldData =>
      val inputName: String = fieldData._1
      val isforFieldName = inputName.endsWith(NamedKey.fieldName)
      if (isforFieldName)
        val inputName: String = fieldData._1
        val fieldKey = FieldKey(inputName)
        val values: Seq[String] = fieldData._2
        val namedKey = NamedKey(fieldKey.key, values.headOption.getOrElse(""))
        Seq(namedKey)
      else
        Seq.empty
    }.toSeq


    //    val namedKeys: Seq[NamedKey] = (for {
    //      tuple <- data
    //      inputName: String = tuple._1
    //      if iii(inputName)
    //      fieldKey = FieldKey(inputName)
    //      values: Seq[String] = tuple._2
    //      keyName <- values.headOption
    //    } yield {
    //      NamedKey(fieldKey.key, keyName)
    //    }).toSeq
    namedKeys

