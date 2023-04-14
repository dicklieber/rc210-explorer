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
import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.datastore.DataStore
import net.wa9nnn.rc210.data.field.FieldEntry
import net.wa9nnn.rc210.data.named.NamedManager
import net.wa9nnn.rc210.key.{KeyFactory, KeyKind}
import net.wa9nnn.rc210.key.KeyFactory.Key
import play.api.mvc._
import play.twirl.api.Html

import javax.inject._

class FieldEditorController @Inject()(val controllerComponents: ControllerComponents,
                                      mappedValues: DataStore
                                     )(implicit enamedManager: NamedManager)
  extends BaseController with LazyLogging {


  def selectKey(): Action[AnyContent] = Action {
    val html: Html = views.html.selectKey()
    Ok(html)
  }

  def editFields(keyKind: KeyKind): Action[AnyContent] = Action {
    val grouped: Seq[(Int, Seq[FieldEntry])] = mappedValues.apply(keyKind)
      .sortBy(_.fieldKey)
      .groupBy(_.fieldKey.key.number)
      .map { case (number, fields) =>
        number -> fields.sortBy(_.fieldKey.fieldName)
      }.toSeq
      .sortBy(_._1)

    val rowSpanForNumber = grouped.head._2.length

    val rows: Seq[Row] = grouped.flatMap { case (number, entries: Seq[FieldEntry]) =>
      entries.tail.map {
        _.toRow()
      }.prepended {
        entries
          .head.
          toRow(Option(Cell(number)
            .withRowSpan(rowSpanForNumber)))
      }
    }
    val table = Table(FieldEntry.header(keyKind), rows)

    Ok(views.html.fieldsEditor(keyKind, table))


  }

  def save(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    var kind: Option[KeyKind] = None
    request.body.asFormUrlEncoded
      .get
      .foreach { case (sKey, values) =>
        try {
          val fieldKey: FieldKey = FieldKey.fromParam(sKey)
          if (kind.isEmpty) kind = Option(fieldKey.key.kind)
          mappedValues.apply(fieldKey, values.headOption.getOrElse(throw new IllegalArgumentException(s"No value for param: $sKey")))
        } catch {
          case e: Exception =>
            logger.error(s"sKey: $sKey values: $values", e)
        }
      }
    Redirect(routes.FieldEditorController.editFields(kind.getOrElse(KeyKind.macroKey)))
  }

}