/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.data.clock.{Clock, DSTPoint, Occurrence}
import net.wa9nnn.rc210.data.datastore.DataStoreActor.UpdateData
import net.wa9nnn.rc210.data.datastore.{DataStoreActor, UpdateCandidate}
import net.wa9nnn.rc210.data.field.Formatters.{MonthOfYearDSTFormatter, OccurrenceFormatter}
import net.wa9nnn.rc210.data.field.{FieldEntry, MonthOfYearDST}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import net.wa9nnn.rc210.serial.comm.{RequestResponse, SerialPortsActor}
import net.wa9nnn.rc210.serial.comm.SerialPortsActor.Message
import net.wa9nnn.rc210.ui.EnumSelect
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc._

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps


@Singleton()
class ClockController @Inject()(actor: ActorRef[DataStoreActor.Message],
                                serialPortsActor: ActorRef[SerialPortsActor.Message])
                               (implicit scheduler: Scheduler, ec: ExecutionContext)
  extends MessagesInjectedController with LazyLogging {
  implicit val timeout: Timeout = 3 seconds


  private val dstPointForm: Mapping[DSTPoint] =
    mapping(
      "month" -> of[MonthOfYearDST],
      "occurrence" -> of[Occurrence]
    )(DSTPoint.apply)(DSTPoint.unapply)

  private val clockForm = Form[Clock](
    mapping(
      "enableDST" -> boolean,
      "hourDST" -> number(min = 0, max = 23),
      "startDST" -> dstPointForm,
      "endDST" -> dstPointForm,
      "say24Hours" -> boolean
    )(Clock.apply)(Clock.unapply)
  )

  def index: Action[AnyContent] = Action.async { implicit request =>
    actor.ask[Seq[FieldEntry]](DataStoreActor.AllForKeyKind(KeyKind.clockKey, _)).map { fieldEntries =>
      val fieldEntry: FieldEntry = fieldEntries.head
      val clock: Clock = fieldEntry.value.asInstanceOf[Clock]
      val filledInForm = clockForm.fill(clock)
      Ok(views.html.clock(filledInForm))
    }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    val formUrlEncoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

    clockForm.bindFromRequest().fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        Future(BadRequest(views.html.clock(formWithErrors)))
      },
      (clock: Clock) => {
        /* binding success, you get the actual value. */
        val updateCandidate: UpdateCandidate = UpdateCandidate(Clock.fieldKey(KeyFactory.clockKey), Right(clock))

        actor.ask[String](UpdateData(Seq(updateCandidate), Seq.empty, who(request), _)).map { _ =>
          Redirect(routes.ClockController.index)
        }
      }
    )
  }

  def setClock(): Action[AnyContent] = Action { implicit request =>

    //    serialPortsActor
    //   clock:  *5100 12 12 23
    // calander:   *5101 06 11 03 Set June 11, 2003 as the current date
    val dt = LocalDateTime.now()
    val clock = s"1*5100${dt.getHour}${dt.getMinute}${dt.getSecond}"
    val calendar = s"1*5101${dt.getMonthValue}${dt.getDayOfMonth}${dt.getYear - 2000}"

    Await.result(serialPortsActor.ask(SerialPortsActor.SendReceive(clock, _)), 2 seconds)
    Await.result(serialPortsActor.ask(SerialPortsActor.SendReceive(calendar, _)), 2 seconds)

    Redirect(routes.IOController.listSerialPorts.url)
  }
}

object ClockController {
  implicit val ocurrenceSelect: EnumSelect[Occurrence] = new EnumSelect[Occurrence]("occurrence")
  implicit val monthOfYearDSTSelect: EnumSelect[MonthOfYearDST] = new EnumSelect[MonthOfYearDST]("month")
}



