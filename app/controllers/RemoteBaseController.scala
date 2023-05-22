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
import net.wa9nnn.rc210.data.clock.Clock
import net.wa9nnn.rc210.data.datastore.{DataStore, UpdateCandidate, UpdateData}
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.remotebase.Mode._
import net.wa9nnn.rc210.data.remotebase.Offset._
import net.wa9nnn.rc210.data.remotebase.Yaesu._
import net.wa9nnn.rc210.data.remotebase._
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.security.authorzation.AuthFilter.h2u
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
@Singleton()
class RemoteBaseController @Inject()(dataStore: DataStore) extends MessagesInjectedController with LazyLogging {


  val rbMemory: Mapping[RBMemory] =
    mapping(
      "frequency" -> text,
      "offset" -> of[Offset],
      "mode" -> of[Mode],
      "ctcssMode" -> of[CtcssMode],
      "ctssCode" -> number,
    )(RBMemory.apply)(RBMemory.unapply)

  val remoteBaseForm = Form[RemoteBase](
    mapping(
      "radio" -> of[Radio],
      "yaesu" -> of[Yaesu],
      "prefix" -> text,
      "memories" -> seq(rbMemory),
    )(RemoteBase.apply)(RemoteBase.unapply)
  )

  def index: Action[AnyContent] = Action { implicit request =>
    val fieldEntry: FieldEntry = dataStore(KeyKind.remoteBaseKey).head

    val remoteBase: RemoteBase = fieldEntry.value.asInstanceOf[RemoteBase]
    val filledInForm = remoteBaseForm.fill(remoteBase)

    Ok(views.html.rermoteBase(filledInForm))
  }

  def save(): Action[AnyContent] = Action { implicit request =>

    remoteBaseForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.rermoteBase(formWithErrors))
      },
      (remoteBase: RemoteBase) => {
        /* binding success, you get the actual value. */
        val updateCandidate = UpdateCandidate(remoteBase.fieldKey, Right(remoteBase))
        val updateData = UpdateData(Seq(updateCandidate))
        dataStore.update(updateData)(h2u(request))
        Redirect(routes.RemoteBaseController.index)
      }
    )
  }

}




