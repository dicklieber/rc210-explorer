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
import net.wa9nnn.rc210
import net.wa9nnn.rc210.data.EditHandler
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.NavMainController
import net.wa9nnn.rc210.*
import play.api.mvc.*
import views.html.NavMain

import javax.inject.{Inject, Singleton}
import scala.collection.immutable

/**
 * Handles editing for all things stored in the [[DataStore]].
 * @param navMain twirl template that provides navigation, menus. A top level frame for any page (except login)
 * @param dataStore where rc-210 info lives.
 * @param components lots of stuff needed for play.
 */
@Singleton()
class EditController @Inject()(navMain: NavMain)
                              (implicit dataStore: DataStore,
                                components: ControllerComponents)
  extends NavMainController(components) with LazyLogging {
  /**
   * A landing page for all data associated with a [[KeyKind]].
   * @param keyKind present all vales for this [[KeyKind]].
   * @return the Http response to send back to the browser.
   */
  def index(keyKind: KeyKind): Action[AnyContent] = Action {
    implicit request => {
      val fieldEntries: Seq[FieldEntry] = dataStore(keyKind)
      Ok(navMain(keyKind, keyKind.handler.index(fieldEntries)))
    }
  }

  /**
   * Edit date for one [[FieldKey]].
   * @param fieldKey edit this one.
   * @return the Http response to send back to the browser.
   */
  def edit(fieldKey: FieldKey): Action[AnyContent] = Action {
    implicit request =>
      val entry: FieldEntry = dataStore.apply(fieldKey)
      Ok(navMain(fieldKey.key.keyKind, fieldKey.editHandler.edit(entry)))
  }

  /**
   * Save the submitted form data to the [[DataStore]].
   * @return the Http response to send back to the browser.
   */
  def save(): Action[AnyContent] = Action{

    implicit request: Request[AnyContent] =>

      given data: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

      given RcSession = request.attrs(sessionKey)

      logger.whenDebugEnabled {
        data.foreach { (name, values) =>
          logger.debug(s"$name: ${values.mkString(", ")}")
        }
      }

      try
        val fieldKey: FieldKey = EditHandler.fieldKey.get
        val handler: EditHandler = fieldKey.key.keyKind.handler
        val updateCandidates: Seq[UpdateCandidate] = handler.bind(data)
        val namedKeys: Seq[NamedKey] = extractNamedKeys(data)

        val candidateAndNames: CandidateAndNames = CandidateAndNames(updateCandidates, namedKeys)
        dataStore.update(candidateAndNames)
        handler.saveOp()
      catch
        case e:Exception =>
          logger.error(s"save: ${e.getMessage}", e)
          InternalServerError(s"request.toString $e.getMessage")
  }

  /**
   * Get all the [[NamedKey]] data from the submitted form.
   * @param data submitted form data fields.
   * @return zero or more named keys. 
   */
  private def extractNamedKeys(data: Map[String, Seq[String]]) =
    val namedKeys: Seq[NamedKey] = (for {
      (inputName: String, values: Seq[String]) <- data
      if inputName.startsWith(NamedKey.fieldName)
      fieldKey = FieldKey(inputName)
      name <- values.headOption
    } yield {
      NamedKey(fieldKey.key, name)
    }).toSeq
    namedKeys

}

object ExtractField:
  def apply[T](name: String, f: String => T)(using encoded: Map[String, Seq[String]]): T =
    (for {
      e <- encoded.get(name)
      v <- e.headOption
    } yield {
      f(v)
    }).getOrElse(throw new IllegalArgumentException(s"No $name in body!"))

