/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.event.bus

import org.beangle.commons.lang.time.DateFormats.UTC

import java.time.Instant

/** 数据总线事件
 */
final case class DataEvent(module: String, typeName: String, ids: Array[String], eventType: DataEventType, updatedAt: Instant, comment: Option[String]) {

  def entityName: String = s"${module}.${typeName}"

  def isMatch(pattern: String): Boolean = {
    pattern == module || module.charAt(pattern.length) == '.' && module.startsWith(pattern)
  }

  override def toString: String = {
    toJson
  }

  def toJson: String = {
    val ids = this.ids.mkString(",")
    this.comment match
      case None =>
        s"""{"entityName":"${this.entityName}","ids":"$ids","eventType":"${this.eventType.toString}","updatedAt":"${UTC.format(java.util.Date.from(this.updatedAt))}"}"""
      case Some(cmt) =>
        s"""{"entityName":"${this.entityName}","ids":"$ids","eventType":"${this.eventType.toString}","comment":"${cmt}","updatedAt":"${UTC.format(java.util.Date.from(this.updatedAt))}"}"""
  }
}

object DataEventType {
  def of(id: Int): DataEventType = fromOrdinal(id - 1)

  def of(name: String): DataEventType = valueOf(name)
}

enum DataEventType {
  case Creation, Update, Deletion

  def id: Int = ordinal + 1
}
