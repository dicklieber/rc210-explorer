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

package net.wa9nnn.rc210.data.datastore

import net.wa9nnn.rc210.KeyKind
import net.wa9nnn.rc210.data.field.{ComplexFieldValue, FieldKey, FieldValue}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout
import play.api.mvc.{Request, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class DataStoreApi @Inject()(implicit actor: ActorRef[DataStoreActor.Message], scheduler: Scheduler, ec: ExecutionContext) {
  implicit val timeout: Timeout = 3 second

  /**
   * @param keyKind do all the [[net.wa9nnn.rc210.data.field.FieldEntry]] for this [[KeyKind]]
   * @param f       function that transforms a Seq[T] to a [[Result]]
   * @tparam T kind of [[FieldValue]] to pass to the f.
   * @return
   */
  def indexValues[T <: FieldValue](keyKind: KeyKind, f: Seq[T] => Result)(implicit request: Request[_]): Future[Result] = {
    for {
      dataStoreReply <- actor.ask(DataStoreMessage(AllForKeyKind(keyKind), _))
    } yield {
      val r: Result = dataStoreReply.forAllValues[T](values => f(values))
      r
    }
  }
  /**
   * @param fieldKey do just this one value.
   * @param f       function that transforms a T to a [[Result]]
   * @tparam T kind of [[FieldValue]] to pass to the f.
   * @return
   */
  def editOne[T <: FieldValue](fieldKey: FieldKey, f: T => Result)(implicit request: Request[_]): Future[Result] = {
    for {
      dataStoreReply <- actor.ask(DataStoreMessage(ForFieldKey(fieldKey), _))
    } yield {
      val r: Result = dataStoreReply.forHead((fieldKey, value) => f(value))
      r
    }
  }
  
  def update(candidateAndNames: CandidateAndNames):Future[String] =
    for {
      dataStoreReply <- actor.ask(DataStoreMessage(candidateAndNames, _))
    } yield
      "done"

}
