package net.wa9nnn.rc210

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.model.DataItem

import scala.language.implicitConversions
import scala.util.Try

/**
 * One section of an RCP .dat file.
 *
 * @param sectionName  from [sectionName]
 * @param dataItems    lines upto next section or end ofd the file.
 */
case class DatSection private(sectionName: String, dataItems: List[DataItem]) extends LazyLogging {
  /**
   * @param f needed to make a T
   * @tparam T create one of these for each [[NumberedValues]].
   * @return
   */
  def process[T](f: NumberedValues => Try[T]): List[T] = {

    val valueM: List[(Option[Int], List[DataItem])] = dataItems.groupBy(_.maybeInt).toList

  val r =   for {
      ns <- valueM
      t <- f(NumberedValues(ns._1, valueMap(ns._2))).toOption
    } yield {
      t
    }
    r
  }

  def valueMap(items: List[DataItem]): Map[String, DataItem] = {
    items.map { dataItem =>
      dataItem.name -> dataItem
    }.toMap(<:<.refl)

  }

  def dump(): Unit = {
    logger.info("Section: {} ({} items)", sectionName, dataItems.size)
    dataItems.foreach {
      _.dump()
    }
  }
}

case class NumberedValues(number: Option[Int], valueMap: Map[String, DataItem])

object NumberedValues {
  def vi(name: String)(implicit numberedValues: NumberedValues): Int = {
    val dataItem: DataItem = numberedValues.valueMap(name)
    java.lang.Integer.parseInt(dataItem.value)
  }

   def vs(name: String)(implicit numberedValues: NumberedValues): String = {
    val dataItem: DataItem = numberedValues.valueMap(name)
    dataItem.value
  }

}