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

import org.beangle.commons.json.{JsonObject, JsonParser}
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.DateFormats.UTC
import org.beangle.event.mq.EventSerializer

class DataEventSerializer extends EventSerializer[DataEvent] {

  def toJson(event: DataEvent): String = {
    event.toJson
  }

  def fromJson(json: String): DataEvent = {
    val emap = JsonParser.parse(json).asInstanceOf[JsonObject]
    val entityName = getString(emap, "entityName")
    val lastDotIndx = entityName.lastIndexOf('.')
    val module = entityName.substring(0, lastDotIndx)
    val typeName = entityName.substring(lastDotIndx + 1)

    val comment = Option(getString(emap, "comment"))
    val eventType = DataEventType.of(getString(emap, "eventType"))
    val updatedAt = UTC.parse(getString(emap, "updatedAt")).toInstant

    val filterStr = getString(emap, "filters")
    val filters =
      if filterStr == null then Map.empty[String, String]
      else
        Strings.split(filterStr, "&").map(x => (Strings.substringBefore(x, "="), Strings.substringAfter(x, "="))).toMap
    DataEvent(module, typeName, filters, eventType, updatedAt, comment)
  }

  private def getString(emap: JsonObject, key: String): String = {
    emap.get(key).orNull.asInstanceOf[String]
  }
}
