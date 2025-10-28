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

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.time.DateFormats.UTC
import org.beangle.event.bus.DataEventType.{Creation, Deletion, Update}

import java.time.Instant

object DataEvent {
  private def getIds(objects: Any): String = {
    objects match {
      case i: Iterable[_] =>
        if i.isEmpty then "*"
        else i.map(x => Properties.get[Any](x, "id").toString).mkString(",")
      case o: Any => Properties.get[Any](o, "id").toString
    }
  }

  private def getEntityName(clazz: Class[_]): String = {
    val className = clazz.getName
    val dollaIdx = className.indexOf('$')
    if dollaIdx == -1 then className else className.substring(0, dollaIdx)
  }

  def create(objects: Any): Iterable[DataEvent] = {
    objects match
      case i: Iterable[_] =>
        i.groupBy(_.getClass) map { case (clazz, values) =>
          DataEvent(getEntityName(clazz), Map("id" -> getIds(values)), Creation, Instant.now, None)
        }
      case o: Any =>
        val className = getEntityName(o.getClass)
        List(DataEvent(className, Map("id" -> getIds(o)), Creation, Instant.now, None))
  }

  def update(clazz: Class[_], filters: Map[String, String], comment: Option[String] = None): DataEvent = {
    val className = getEntityName(clazz)
    DataEvent(className, filters, Update, Instant.now, comment)
  }

  def update(objects: Any): Iterable[DataEvent] = {
    objects match
      case i: Iterable[_] =>
        i.groupBy(_.getClass) map { case (clazz, values) =>
          update(clazz, Map("id" -> getIds(values)))
        }
      case o: Any =>
        List(update(o.getClass, Map("id" -> getIds(o))))
  }

  def remove(objects: Any): Iterable[DataEvent] = {
    objects match
      case i: Iterable[_] =>
        i.groupBy(_.getClass) map { case (clazz, values) =>
          DataEvent(getEntityName(clazz), Map("id" -> getIds(values)), Deletion, Instant.now, None)
        }
      case o: Any =>
        val className = getEntityName(o.getClass)
        List(DataEvent(className, Map("id" -> getIds(o)), Deletion, Instant.now, None))
  }
}

/** 数据总线事件
 */
final case class DataEvent(dataType: String, filters: Map[String, String], eventType: DataEventType,
                           updatedAt: Instant, comment: Option[String]) {

  def isMatch(pattern: String): Boolean = {
    val m = moduleName
    pattern == m || m.startsWith(pattern) && m.charAt(pattern.length) == '.'
  }

  def hasFilter(name: String, value: String): Boolean = {
    filters.getOrElse(name, "*") == value
  }

  override def toString: String = {
    toJson
  }

  def moduleName: String = {
    val lastDotIdx = dataType.lastIndexOf('.')
    if lastDotIdx < 0 then "" else dataType.substring(0, lastDotIdx)
  }

  def toJson: String = {
    val filterString = filters.map(x => s"${x._1}=${x._2}").mkString("&")
    this.comment match
      case None =>
        s"""{"dataType":"${this.dataType}","filters":"${filterString}","eventType":"${this.eventType.toString}","updatedAt":"${UTC.format(java.util.Date.from(this.updatedAt))}"}"""
      case Some(cmt) =>
        s"""{"dataType":"${this.dataType}","filters":"${filterString}","eventType":"${this.eventType.toString}","comment":"${cmt}","updatedAt":"${UTC.format(java.util.Date.from(this.updatedAt))}"}"""
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
