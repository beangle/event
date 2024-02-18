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

import com.google.gson.GsonBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.DateFormats.UTC
import org.beangle.event.mq.EventSerializer

class DataEventSerializer extends EventSerializer[DataEvent] {
  private val gson = new GsonBuilder().create()

  def toJson(event: DataEvent): String = {
    event.toJson
  }

  def fromJson(json: String): DataEvent = {
    val emap = gson.fromJson(json, classOf[java.util.HashMap[String, String]])
    val entityName = emap.get("entityName")
    val lastDotIndx = entityName.lastIndexOf('.')
    val module = entityName.substring(0, lastDotIndx)
    val typeName = entityName.substring(lastDotIndx + 1)

    val comment = Option(emap.get("comment"))
    val eventType = DataEventType.of(emap.get("eventType"))
    val updatedAt = UTC.parse(emap.get("updatedAt")).toInstant

    DataEvent(module, typeName, Strings.split(emap.get("ids")),
      eventType, updatedAt, comment)
  }
}
