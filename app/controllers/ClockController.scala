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

import com.typesafe.scalalogging.LazyLogging
import controllers.ClockController.{monthOfYearDSTSelect, ocurrenceSelect}
import net.wa9nnn.rc210.data.clock.{Clock, DSTPoint, Occurrence}
import net.wa9nnn.rc210.data.datastore.{UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.{FieldEntry, MonthOfYearDST}
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.who
import net.wa9nnn.rc210.ui.EnumSelect
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc._

import javax.inject.{Inject, Singleton}

@Singleton()
class ClockController @Inject()(dataStore: DataStore) extends MessagesInjectedController with LazyLogging {


  val dstPointForm: Mapping[DSTPoint] =
    mapping(
      "month" -> of[MonthOfYearDST],
      "occurrence" -> of[Occurrence]
    )(DSTPoint.apply)(DSTPoint.unapply)

  val clockForm = Form[Clock](
    mapping(
      "enableDST" -> boolean,
      "hourDST" -> number(min = 0, max = 23),
      "startDST" -> dstPointForm,
      "endDST" -> dstPointForm,
      "say24Hours" -> boolean
    )(Clock.apply)(Clock.unapply)
  )

  def index: Action[AnyContent] = Action { implicit request =>
    val fieldEntry: FieldEntry = dataStore(KeyKind.clockKey).head

    val clock: Clock = fieldEntry.value.asInstanceOf[Clock]
    val filledInForm = clockForm.fill(clock)

    Ok(views.html.clock(filledInForm))
  }

  def save(): Action[AnyContent] = Action { implicit request =>

    clockForm.bindFromRequest().fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.clock(formWithErrors))
      },
      (clock: Clock) => {
        /* binding success, you get the actual value. */
        val updateCandidate = UpdateCandidate(Clock.fieldKey(KeyFactory.clockKey), Right(clock))
        val updateData = UpdateData(Seq(updateCandidate))
        dataStore.update(updateData)(who(request))
        Redirect(routes.ClockController.index)
      }
    )
  }

  def setClock: Action[AnyContent] = Action { implicit request =>
    Ok("todo set clock in RC-210")
  }
}

object ClockController {
  implicit val ocurrenceSelect: EnumSelect[Occurrence] = new EnumSelect[Occurrence]("occurrence")
  implicit val monthOfYearDSTSelect: EnumSelect[MonthOfYearDST] = new EnumSelect[MonthOfYearDST]("month")
}



