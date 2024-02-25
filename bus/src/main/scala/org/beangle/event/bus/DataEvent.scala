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
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.DateFormats.UTC
import org.beangle.event.bus.DataEventType.{Creation, Deletion, Update}

import java.time.Instant

object DataEvent {
  private def getId(objects: Any): String = {
    objects match {
      case i: Iterable[_] =>
        if i.isEmpty then "*"
        else i.map(x => Properties.get(x, "id").toString).mkString(",")
      case o: Any => Properties.get(o, "id").toString
    }
  }

  def create(objects: Any): Iterable[DataEvent] = {
    objects match
      case i: Iterable[_] =>
        i.groupBy(_.getClass.getName) map { case (className, values) =>
          val module = Strings.substringBeforeLast(className, ".")
          val typeName = Strings.substringAfterLast(className, ".")
          val id = getId(values)
          DataEvent(module, typeName, id, Creation, Instant.now, None)
        }
      case o: Any =>
        val className = o.getClass.getName
        val module = Strings.substringBeforeLast(className, ".")
        val typeName = Strings.substringAfterLast(className, ".")
        val id = getId(o)
        List(DataEvent(module, typeName, id, Creation, Instant.now, None))
  }

  def update(clazz: Class[_], id: String, comment: Option[String] = None): DataEvent = {
    val className = clazz.getName
    val module = Strings.substringBeforeLast(className, ".")
    val typeName = Strings.substringAfterLast(className, ".")
    DataEvent(module, typeName, id, Update, Instant.now, comment)
  }

  def update(objects: Any): Iterable[DataEvent] = {
    objects match
      case i: Iterable[_] =>
        i.groupBy(_.getClass) map { case (clazz, values) =>
          update(clazz, getId(values))
        }
      case o: Any =>
        List(update(o.getClass, getId(o)))
  }

  def remove(objects: Any): Iterable[DataEvent] = {
    objects match
      case i: Iterable[_] =>
        i.groupBy(_.getClass.getName) map { case (className, values) =>
          val module = Strings.substringBeforeLast(className, ".")
          val typeName = Strings.substringAfterLast(className, ".")
          val id = getId(values)
          DataEvent(module, typeName, id, Deletion, Instant.now, None)
        }
      case o: Any =>
        val className = o.getClass.getName
        val module = Strings.substringBeforeLast(className, ".")
        val typeName = Strings.substringAfterLast(className, ".")
        val id = getId(o)
        List(DataEvent(module, typeName, id, Deletion, Instant.now, None))
  }
}

/** 数据总线事件
  */
final case class DataEvent(module: String, typeName: String, id: String, eventType: DataEventType, updatedAt: Instant, comment: Option[String]) {

  def entityName: String = s"${module}.${typeName}"

  def isMatch(pattern: String): Boolean = {
    pattern == module || module.charAt(pattern.length) == '.' && module.startsWith(pattern)
  }

  override def toString: String = {
    toJson
  }

  def toJson: String = {
    this.comment match
      case None =>
        s"""{"entityName":"${this.entityName}","id":"${this.id}","eventType":"${this.eventType.toString}","updatedAt":"${UTC.format(java.util.Date.from(this.updatedAt))}"}"""
      case Some(cmt) =>
        s"""{"entityName":"${this.entityName}","id":"${this.id}","eventType":"${this.eventType.toString}","comment":"${cmt}","updatedAt":"${UTC.format(java.util.Date.from(this.updatedAt))}"}"""
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
