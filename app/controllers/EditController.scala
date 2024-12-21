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
import net.wa9nnn.rc210.data.datastore.*
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.security.authentication.RcSession
import net.wa9nnn.rc210.security.authorzation.AuthFilter.sessionKey
import net.wa9nnn.rc210.ui.{FormData, NamedKeyExtractor, NamedKeyManager, NavMainController}
import net.wa9nnn.rc210.*
import play.api.mvc.*
import play.api.mvc.Results.Ok
import views.html.NavMain

import javax.inject.{Inject, Singleton}
import scala.collection.immutable
import scala.language.postfixOps

/**
 * Handles editing for all things stored in the [[DataStore]].
 *
 * @param navMain    twirl template that provides navigation, menus. A top level frame for any page (except login)
 * @param dataStore  where rc-210 info lives.
 * @param components lots of stuff needed for play.
 */
@Singleton()
class EditController @Inject()(navMain: NavMain)
                              (implicit dataStore: DataStore,
                               namedKeyManager: NamedKeyManager,
                               components: ControllerComponents)
  extends NavMainController(components) with LazyLogging {
  /**
   * A landing page for all data associated with a [[KeyMetadata]].
   *
   * @param keyKind present all vales for this [[KeyMetadata]].
   * @return the Http response to send back to the browser.
   */
  def index(keyKind: KeyMetadata): Action[AnyContent] = Action {
    implicit request => {
      val fieldEntries: Seq[FieldEntry] = dataStore(keyKind)
      Ok(navMain(keyKind, keyKind.handler.index(fieldEntries)))
    }
  }

  /**
   * Edit data for one [[Key]].
   *
   * @param key edit this one.
   * @return the Http response to send back to the browser.
   */
  def edit(key:Key): Action[AnyContent] = Action {
    implicit request =>
      val fieldEntry: FieldEntry = dataStore.getFieldEntry(key)
      Ok(navMain(key.keyMetadata, key.keyMetadata.handler.edit(fieldEntry)))
  }

  /**
   * Save the submitted form data to the [[DataStore]].
   *
   * @return the Http response to send back to the browser.
   */
  def save(): Action[AnyContent] = Action {

    implicit request: Request[AnyContent] =>

      val formData: FormData = FormData()

      given RcSession = request.attrs(sessionKey)

      namedKeyManager.saveNamedKeys(formData)

      try
        namedKeyManager.saveNamedKeys(formData)
        val key  = formData.key
        val updateCandidates = key.bind(formData)

        dataStore.update(updateCandidates)
        key.saveOp(key.keyMetadata)
      catch
        case e: Exception =>
          logger.error(s"save: ${e.getMessage}", e)
          InternalServerError(s"request.toString $e.getMessage")
  }

}

object ExtractField:
  def apply[T](name: String, f: String => T)(using encoded: Map[String, Seq[String]]): T =
    (for {
      e <- encoded.get(name)
      v <- e.headOption
    } yield {
      f(v)
    }).getOrElse(throw new IllegalArgumentException(s"No $name in body!"))

