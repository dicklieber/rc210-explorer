package net.wa9nnn.rc210

import net.wa9nnn.model.DataItem

import java.nio.file.Paths

object RC210 extends App {

  //  private val datFile: DatFile = DatFileIo.read(Paths.get("data/SARA_KA9HHH_20230105_1701.dat"))
  private val datFile: DatFile = DatFileIo.apply(Paths.get("test/resources/data/01162023.dat"))
  //  datFile.dump()


  def extractSchedules(): Seq[Schedule] = {
    for {
      pair: (Option[Int], Seq[DataItem]) <- datFile.section("Scheduler").dataItems
        .groupBy(_.maybeInt)
        .toSeq
        .sortBy(_._1)
      if pair._1.isDefined // has number
      schedule <- Schedule.buildSchdule(pair._1.get, pair._2).toOption
    } yield {
      schedule
    }
  }

  val schedules: Seq[Schedule] = extractSchedules()
  println(schedules)

}


